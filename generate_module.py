#!/usr/bin/env python3
"""
generate_module.py - Hybrid interactive + CLI generator (refactored)
Now supports: OneToOne, OneToMany, ManyToOne, ManyToMany
Features:
 - Hybrid mode: CLI args OR interactive prompts
 - Entities extend BaseEntity (import path as requested)
 - DTOs exclude relationships (keeps DTOs simple)
 - ManyToMany => Set + @JoinTable
 - OneToMany => List + mappedBy (owner is inverse)
 - OneToOne => owning side with @JoinColumn by default
 - Relation parsing: rel:<RelationType>:TargetEntity:fieldName[:options]
   e.g. rel:ManyToOne:User:user
         rel:OneToMany:Order:orders:mappedBy=customer
         rel:OneToOne:Profile:profile:owner=false  (owner=false -> inverse side mappedBy=profile)
 - Service & Controller boilerplate preserved
"""

import os
import sys
from textwrap import dedent

# ---------------- CONFIG ----------------
PACKAGE_ROOT = "tetish_inn_backend.tetish_inn.modules"
SRC_BASE = os.path.join("src", "main", "java")
MODULES_BASE_PATH = os.path.join(SRC_BASE, *PACKAGE_ROOT.split("."))

API_RESPONSE_IMPORT = "tetish_inn_backend.tetish_inn.common.utils.ApiResponse"
PAGINATION_REQUEST_IMPORT = "tetish_inn_backend.tetish_inn.common.utils.PaginationRequest"
PAGINATED_RESPONSE_IMPORT = "tetish_inn_backend.tetish_inn.common.utils.PaginatedResponse"

BASE_ENTITY_IMPORT = "tetish_inn_backend.tetish_inn.modules.audit.BaseEntity"

# Allowed types presented to user and mapping to (Type, optional import)
ALLOWED_TYPES = [
    ("string", ("String", None)),
    ("integer", ("Integer", None)),
    ("boolean", ("Boolean", None)),
    ("bigdecimal", ("BigDecimal", "java.math.BigDecimal")),
    ("uuid", ("UUID", "java.util.UUID")),
    ("text", ("String", "jakarta.persistence.Lob")),
    ("long", ("Long", None)),
    ("float", ("Float", None)),
    ("double", ("Double", None)),
    ("localdate", ("LocalDate", "java.time.LocalDate")),
    ("localtime", ("LocalTime", "java.time.LocalTime")),
    ("localdatetime", ("LocalDateTime", "java.time.LocalDateTime")),
    ("datetime", ("LocalDateTime", "java.time.LocalDateTime")),
    ("timestamp", ("LocalDateTime", "java.time.LocalDateTime")),
    ("date", ("Date", "java.util.Date")),
    ("json", ("String", None)),
    ("int", ("Integer", None)),
    ("bool", ("Boolean", None)),
]

# helper maps
TYPE_DISPLAY = [t[0] for t in ALLOWED_TYPES]
TYPE_MAP = {t[0]: t[1] for t in ALLOWED_TYPES}

# ---------------------------------------

def ensure_dir(p):
    os.makedirs(p, exist_ok=True)

def write_file(path, content):
    ensure_dir(os.path.dirname(path))
    with open(path, "w", encoding="utf-8") as f:
        f.write(dedent(content).strip() + "\n")

def camel_lower(s):
    return s[0].lower() + s[1:] if s else s

def snake_to_camel(s):
    parts = s.split("_")
    return "".join(p.capitalize() for p in parts)

def singularize(name):
    # simple heuristic: remove trailing 's' from last part if present
    parts = name.split("_")
    last = parts[-1]
    if len(last) > 1 and last.endswith("s"):
        parts[-1] = last[:-1]
    return "_".join(parts)

def table_name_from_module(module_input):
    parts = module_input.split("_")
    last = parts[-1]
    if not last.endswith("s"):
        parts[-1] = last + "s"
    return "_".join(parts)

def parse_arg(arg):
    """
    Parse a CLI argument. Two forms:
     - field:type[:flags...]  e.g. totalAmount:bigdecimal
     - rel:<Relation>:TargetEntity:fieldName[:opt1=val,...]
       e.g. rel:ManyToOne:User:user
             rel:OneToMany:Order:orders:mappedBy=customer
             rel:OneToOne:Profile:profile:owner=false
    """
    if arg.startswith("rel:"):
        parts = arg.split(":", 4)
        # allow optional options after 4th colon (as a single string)
        if len(parts) < 4:
            raise ValueError("Relation format: rel:<RelationType>:TargetEntity:fieldName[:options]")
        relation = parts[1]
        target = parts[2]
        field = parts[3]
        opts_raw = parts[4] if len(parts) == 5 else ""
        opts = {}
        if opts_raw:
            for o in opts_raw.split(","):
                if "=" in o:
                    k, v = o.split("=", 1)
                    opts[k.strip()] = v.strip()
                else:
                    opts[o.strip()] = "true"
        return {"relation": relation, "target": target, "field": field, "opts": opts}
    else:
        parts = arg.split(":")
        name = parts[0]
        typ = parts[1] if len(parts) > 1 else "string"
        flags = parts[2:] if len(parts) > 2 else []
        return {"field": name, "type": typ, "flags": flags}

def resolve_type(typ):
    t = typ.lower()
    if t in TYPE_MAP:
        return TYPE_MAP[t]  # (TypeName, import)
    return (typ, None)

# ---------------- BUILDERS ----------------

def build_dto_request(pkg, entity, fields):
    imports = set()
    lines = []
    for f in fields:
        tname, imp = resolve_type(f["type"])
        if imp:
            imports.add(imp)
        lines.append(f"    private {tname} {f['field']};")
    imports_block = ""
    if any(im == "java.math.BigDecimal" for im in imports):
        imports_block += "import java.math.BigDecimal;\n"
    if "jakarta.persistence.Lob" in imports:
        imports_block += "import jakarta.persistence.Lob;\n"
    return f"""
    package {pkg}.dto;

    import lombok.Data;
    {imports_block}
    @Data
    public class {entity}RequestDTO {{
{os.linesep.join(lines)}
    }}
    """

def build_dto_response(pkg, entity, fields):
    imports = set(["java.util.UUID"])
    lines = ["    private UUID id;"]
    for f in fields:
        tname, imp = resolve_type(f["type"])
        if imp:
            imports.add(imp)
        lines.append(f"    private {tname} {f['field']};")
    imports_block = ""
    if "java.math.BigDecimal" in imports:
        imports_block += "import java.math.BigDecimal;\n"
    if "jakarta.persistence.Lob" in imports:
        imports_block += "import jakarta.persistence.Lob;\n"
    imports_block += "import java.util.UUID;\n"
    return f"""
    package {pkg}.dto;

    import lombok.Data;
    {imports_block}
    @Data
    public class {entity}ResponseDTO {{
{os.linesep.join(lines)}
    }}
    """

def build_entity(pkg, entity, table_name, fields, relations):
    """
    Build entity class content. Relations is a list of dicts:
      { relation: 'OneToOne'|'OneToMany'|'ManyToOne'|'ManyToMany',
        target: 'User',
        field: 'user' or 'orders',
        opts: {...} (optional)
      }
    Strategy:
     - OneToOne: by default create owning side with @JoinColumn(name = "<field>_id")
               if opts.get("owner") == "false" then create inverse side with mappedBy option
     - ManyToOne: owning side -> @ManyToOne + @JoinColumn(name = "<field>_id")
     - OneToMany: inverse side, create List<Target> field, require opts.get("mappedBy") or infer mappedBy = camelLower(currentEntity)
     - ManyToMany: create Set<Target> with @ManyToMany + @JoinTable (owning side). If opts has mappedBy, then it's inverse side.
    """
    import_lines = [
        "import jakarta.persistence.*;",
        "import lombok.Getter;",
        "import lombok.Setter;",
        "import java.util.UUID;",
        f"import {BASE_ENTITY_IMPORT};"
    ]
    needs_list = any(r["relation"].lower() == "onetomany" for r in relations)
    needs_set = any(r["relation"].lower() == "manytomany" for r in relations)
    if needs_list:
        import_lines.append("import java.util.List;")
        import_lines.append("import java.util.ArrayList;")
    if needs_set:
        import_lines.append("import java.util.Set;")
        import_lines.append("import java.util.HashSet;")
    # include specific imports from field types
    for f in fields:
        _, imp = resolve_type(f["type"])
        if imp and imp not in import_lines:
            import_lines.append(f"import {imp};")
    # add common JPA imports for cascade/fetch if not covered (jakarta.persistence.* already present)
    lines = []
    # id - Note: BaseEntity might already include audit fields. But you asked entity extends BaseEntity; keep UUID id here if needed.
    # We'll include id as primary key if BaseEntity doesn't define it — but safer to include since original script had it.
    lines.append("    @Id")
    lines.append("    @GeneratedValue(strategy = GenerationType.AUTO)")
    lines.append("    private UUID id;")
    lines.append("")
    # add basic fields
    for f in fields:
        tname, _ = resolve_type(f["type"])
        lines.append(f"    private {tname} {f['field']};")
    # add relations
    for r in relations:
        rel = r["relation"]
        rel_low = rel.lower()
        tgt = r["target"]
        fld = r["field"]
        opts = r.get("opts", {}) or {}
        # Owner inference helpers
        current_entity_lc = entity.lower()
        inferred_mapped_by = opts.get("mappedBy") or camel_lower(entity)
        # ONE TO ONE
        if rel_low == "onetoone":
            # If opts.owner == "false" -> inverse side: mappedBy=opts.mappedBy or inferred
            owner_flag = opts.get("owner", "true").lower() != "false"
            if owner_flag:
                # owning side: has @JoinColumn
                lines.append("")
                lines.append("    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)")
                join_col = opts.get("joinColumn", f"{fld}_id")
                lines.append(f"    @JoinColumn(name = \"{join_col}\")")
                lines.append(f"    private {tgt} {fld};")
            else:
                # inverse side
                mapped = opts.get("mappedBy", inferred_mapped_by)
                lines.append("")
                lines.append(f"    @OneToOne(mappedBy = \"{mapped}\", fetch = FetchType.LAZY)")
                lines.append(f"    private {tgt} {fld};")
        # MANY TO ONE
        elif rel_low == "manytoone":
            lines.append("")
            lines.append("    @ManyToOne(fetch = FetchType.LAZY)")
            join_col = opts.get("joinColumn", f"{fld}_id")
            lines.append(f"    @JoinColumn(name = \"{join_col}\")")
            lines.append(f"    private {tgt} {fld};")
        # ONE TO MANY
        elif rel_low == "onetomany":
            # OneToMany is usually inverse side. We require mappedBy either from opts or guess.
            mapped = opts.get("mappedBy")
            if not mapped:
                # infer mappedBy as camelLower of current entity (owner field in target)
                mapped = camel_lower(entity)
            collection_field = fld if fld.endswith("s") else (fld + "s")
            lines.append("")
            lines.append(f"    @OneToMany(mappedBy = \"{mapped}\", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)")
            lines.append(f"    private List<{tgt}> {collection_field} = new ArrayList<>();")
        # MANY TO MANY
        elif rel_low == "manytomany":
            # if opts has mappedBy -> inverse side, otherwise owning side with @JoinTable
            mapped = opts.get("mappedBy")
            collection_field = fld if fld.endswith("s") else (fld + "s")
            if mapped:
                lines.append("")
                lines.append(f"    @ManyToMany(mappedBy = \"{mapped}\", fetch = FetchType.LAZY)")
                lines.append(f"    private Set<{tgt}> {collection_field} = new HashSet<>();")
            else:
                jt_name = opts.get("joinTable", f"{entity.lower()}_{tgt.lower()}")
                lines.append("")
                lines.append("    @ManyToMany(fetch = FetchType.LAZY)")
                lines.append("    @JoinTable(")
                lines.append(f"        name = \"{jt_name}\",")
                lines.append(f"        joinColumns = @JoinColumn(name = \"{entity.lower()}_id\"),")
                lines.append(f"        inverseJoinColumns = @JoinColumn(name = \"{tgt.lower()}_id\")")
                lines.append("    )")
                lines.append(f"    private Set<{tgt}> {collection_field} = new HashSet<>();")
        else:
            # unsupported => just add raw field
            lines.append("")
            lines.append(f"    // Unsupported relation '{rel}' — added as raw field.")
            lines.append(f"    private {r.get('target', 'Object')} {fld};")

    import_block = "\n".join(sorted(set(import_lines)))
    return f"""
    package {pkg};

    {import_block}

    @Getter
    @Setter
    @Entity
    @Table(name = "{table_name}")
    public class {entity} extends BaseEntity {{

{os.linesep.join(lines)}
    }}
    """

def build_repository(pkg, entity):
    return f"""
    package {pkg};

    import org.springframework.data.jpa.repository.JpaRepository;
    import java.util.UUID;

    public interface {entity}Repository extends JpaRepository<{entity}, UUID> {{
    }}
    """

def build_mapstruct_mapper(pkg, entity, fields):
    imports = [
        "import org.mapstruct.Mapper;",
        "import org.mapstruct.MappingTarget;",
        "import org.mapstruct.ReportingPolicy;",
        f"import {pkg}.dto.{entity}RequestDTO;",
        f"import {pkg}.dto.{entity}ResponseDTO;",
        # f"import {pkg}.{entity};"
    ]
    imports_block = "\n".join(sorted(imports))
    return f"""
    package {pkg};

    {imports_block}

    @Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
    public interface {entity}Mapper {{

        {entity} toEntity({entity}RequestDTO dto);

        void updateEntityFromDto({entity}RequestDTO dto, @MappingTarget {entity} entity);

        {entity}ResponseDTO toDto({entity} entity);
    }}
    """

def build_service(pkg, entity):
    imports = [
        "import org.springframework.stereotype.Service;",
        "import lombok.RequiredArgsConstructor;",
        "import org.springframework.http.ResponseEntity;",
        "import java.util.UUID;",
        "import java.util.Optional;",
        "import org.springframework.data.domain.Page;",
        "import org.springframework.data.domain.Pageable;",
        "import org.springframework.data.domain.PageRequest;",
        "import org.springframework.data.domain.Sort;",
        f"import {API_RESPONSE_IMPORT};",
        f"import {pkg}.dto.{entity}RequestDTO;",
        f"import {pkg}.dto.{entity}ResponseDTO;",
        # f"import {pkg}.{entity};",
        # f"import {pkg}.{entity}Repository;",
        # f"import {pkg}.{entity}Mapper;",
        f"import {PAGINATION_REQUEST_IMPORT};",
        f"import {PAGINATED_RESPONSE_IMPORT};",
    ]
    # keep imports sorted and unique; also strip accidental trailing braces
    imports = sorted(set(i.replace("};", ";") for i in imports))
    imports_block = "\n".join(imports)
    repo_field = f"    private final {entity}Repository {camel_lower(entity)}Repository;"
    mapper_field = f"    private final {entity}Mapper {camel_lower(entity)}Mapper;"
    # saveOrUpdate (no special relation processing; relations persist via JPA cascade / owner handling)
    save_block = dedent(f"""
        {entity} entity;
        if (id != null) {{
            Optional<{entity}> existing = {camel_lower(entity)}Repository.findById(id);
            if (existing.isPresent()) {{
                entity = existing.get();
                {camel_lower(entity)}Mapper.updateEntityFromDto(request, entity);
            }} else {{
                return ResponseEntity.notFound().build();
            }}
        }} else {{
            entity = {camel_lower(entity)}Mapper.toEntity(request);
        }}

        {camel_lower(entity)}Repository.save(entity);
        return ResponseEntity.ok(ApiResponse.success("{entity} saved successfully", (Object)entity));
    """).rstrip()

    getbyid_block = dedent(f"""
        return {camel_lower(entity)}Repository.findById(id)
                .map(e -> ResponseEntity.ok(ApiResponse.success("{entity} fetched", (Object)e)))
                .orElse(ResponseEntity.notFound().build());
    """).rstrip()

    delete_block = dedent(f"""
        return {camel_lower(entity)}Repository.findById(id)
                .map(e -> {{
                    {camel_lower(entity)}Repository.delete(e);
                    return ResponseEntity.ok(ApiResponse.success("{entity} deleted", null));
                }}).orElse(ResponseEntity.notFound().build());
    """).rstrip()

    getall_block = dedent(f"""
        int start = Math.max(request.getStart(), 0);
        int limit = request.getLimit() > 0 ? request.getLimit() : 10;

        Pageable pageable = PageRequest.of(start / limit, limit, Sort.by("createdAt").descending());
        Page<{entity}> page = {camel_lower(entity)}Repository.findAll(pageable);
        Page<{entity}ResponseDTO> mapped = page.map({camel_lower(entity)}Mapper::toDto);

        return new PaginatedResponse<>(mapped.getContent(), mapped.getTotalPages());
    """).rstrip()

    return f"""
    package {pkg};

    {imports_block}

    @Service
    @RequiredArgsConstructor
    public class {entity}Service {{

{repo_field}
{mapper_field}

        public ResponseEntity<ApiResponse<Object>> saveOrUpdate(UUID id, {entity}RequestDTO request) {{
{save_block}
        }}

        public ResponseEntity<ApiResponse<Object>> getById(UUID id) {{
{getbyid_block}
        }}

        public ResponseEntity<ApiResponse<Object>> delete(UUID id) {{
{delete_block}
        }}

        public PaginatedResponse<{entity}ResponseDTO> getAll(PaginationRequest request) {{
{getall_block}
        }}
    }}
    """

def build_controller(pkg, entity):
    module_lower = pkg.split(".")[-1]
    imports = [
        "import org.springframework.web.bind.annotation.*;",
        "import org.springframework.http.ResponseEntity;",
        "import java.util.UUID;",
        "import lombok.RequiredArgsConstructor;",
        # f"import {pkg}.{entity}Service;",
        f"import {pkg}.dto.{entity}RequestDTO;",
        f"import {pkg}.dto.{entity}ResponseDTO;",
        f"import {API_RESPONSE_IMPORT};",
        f"import {PAGINATION_REQUEST_IMPORT};",
        f"import {PAGINATED_RESPONSE_IMPORT};",
    ]
    imports_block = "\n".join(sorted(set(imports)))
    return f"""
    package {pkg};

    {imports_block}

    @RestController
    @RequestMapping("/api/{module_lower}")
    @RequiredArgsConstructor
    public class {entity}Controller {{

        private final {entity}Service service;

        @PostMapping("/save-or-update")
        public ResponseEntity<ApiResponse<Object>> saveOrUpdate(@RequestBody {entity}RequestDTO request) {{
            return service.saveOrUpdate(null, request);
        }}

        @PostMapping("/all")
        public PaginatedResponse<{entity}ResponseDTO> getAll(@RequestBody PaginationRequest request) {{
            return service.getAll(request);
        }}

        @GetMapping("/{'{'}id{'}'}")
        public ResponseEntity<ApiResponse<Object>> getById(@PathVariable UUID id) {{
            return service.getById(id);
        }}

        @DeleteMapping("/{'{'}id{'}'}")
        public ResponseEntity<ApiResponse<Object>> delete(@PathVariable UUID id) {{
            return service.delete(id);
        }}
    }}
    """

# ---------------- INTERACTIVE HELPERS ----------------

def prompt(prompt_text, default=None):
    if default:
        val = input(f"{prompt_text} [{default}]: ").strip()
        return val if val else default
    else:
        return input(f"{prompt_text}: ").strip()

def choose_type_interactive():
    print("Choose field type:")
    for i, td in enumerate(TYPE_DISPLAY, start=1):
        print(f"  {i}) {td}")
    while True:
        sel = input("Select type number: ").strip()
        if sel.isdigit() and 1 <= int(sel) <= len(TYPE_DISPLAY):
            return TYPE_DISPLAY[int(sel)-1]
        print("Invalid selection. Pick a number.")

def interactive_flow():
    print("=== Interactive Module Generator ===")
    raw_module = prompt("Enter module name (snake_case, singular preferred, e.g. user or user_profile)")
    module_norm = singularize(raw_module.lower())
    entity_name = snake_to_camel(module_norm)
    table_name = table_name_from_module(module_norm)

    print(f"Module -> package fragment: {module_norm}")
    print(f"Entity class name: {entity_name}")
    print(f"DB table name: {table_name}")

    fields = []
    relations = []
    while True:
        add = prompt("Add a field? (y/n)", "y").lower()
        if add not in ("y", "yes"):
            break
        fname = prompt("Field name (camelCase recommended, e.g. usrName)")
        ftype = choose_type_interactive()
        fields.append({"field": fname, "type": ftype, "flags": []})

    while True:
        add_rel = prompt("Add a relationship? (y/n)", "n").lower()
        if add_rel not in ("y", "yes"):
            break
        print("Relation types: 1) OneToOne  2) OneToMany  3) ManyToOne  4) ManyToMany")
        rsel = input("Select relation type [1/2/3/4]: ").strip()
        mapping = {"1": "OneToOne", "2": "OneToMany", "3": "ManyToOne", "4": "ManyToMany"}
        rtype = mapping.get(rsel, "ManyToOne")
        target = prompt("Target entity name (CamelCase, e.g. User)")
        # default field suggestion
        default_field = target[0].lower() + target[1:]
        if rtype in ("OneToMany", "ManyToMany"):
            default_field = default_field + "s"
        field_suggest = prompt("Field name for this relation (e.g. user)", default_field)
        opts = {}
        # extra options
        if rtype == "OneToMany":
            mapped = prompt("mappedBy in target entity (leave blank to infer)", "")
            if mapped:
                opts["mappedBy"] = mapped
        if rtype == "ManyToMany":
            inverse = prompt("Is this the inverse side? (y/n)", "n").lower()
            if inverse in ("y", "yes"):
                mm_mapped_by = prompt("mappedBy value on owning side (e.g. tags)", "")
                if mm_mapped_by:
                    opts["mappedBy"] = mm_mapped_by
            else:
                jt = prompt("Join table name (leave blank for default)", "")
                if jt:
                    opts["joinTable"] = jt
        if rtype == "OneToOne":
            owner = prompt("Should this be the owning side? (y/n)", "y").lower()
            if owner in ("n", "no"):
                opts["owner"] = "false"
            else:
                jc = prompt("JoinColumn name (leave blank for default)", "")
                if jc:
                    opts["joinColumn"] = jc
        relations.append({"relation": rtype, "target": target, "field": field_suggest, "opts": opts})

    return {
        "module": module_norm,
        "entity": entity_name,
        "table": table_name,
        "fields": fields,
        "relations": relations
    }

# ---------------- MAIN ----------------

def main():
    # ensure modules base present
    if not os.path.isdir(MODULES_BASE_PATH):
        print(f"Error: modules base path not found: {MODULES_BASE_PATH}")
        sys.exit(1)

    if len(sys.argv) == 1:
        # interactive
        result = interactive_flow()
        module = result["module"]
        entity = result["entity"]
        table = result["table"]
        fields = result["fields"]
        relations = result["relations"]
    else:
        # CLI mode
        raw = sys.argv[1:]
        module = raw[0]
        module = singularize(module.lower())
        entity = snake_to_camel(module)
        table = table_name_from_module(module)
        fields = []
        relations = []
        for arg in raw[1:]:
            parsed = parse_arg(arg)
            if "field" in parsed and "type" in parsed and "relation" not in parsed:
                fields.append({"field": parsed["field"], "type": parsed["type"], "flags": parsed.get("flags", [])})
            elif "relation" in parsed:
                relations.append(parsed)

    module_lower = module
    module_pkg = PACKAGE_ROOT + "." + module_lower
    module_path = os.path.join(MODULES_BASE_PATH, module_lower)
    ensure_dir(module_path)
    dto_path = os.path.join(module_path, "dto")
    ensure_dir(dto_path)

    # 1) DTOs
    write_file(os.path.join(dto_path, f"{entity}RequestDTO.java"), build_dto_request(module_pkg, entity, fields))
    write_file(os.path.join(dto_path, f"{entity}ResponseDTO.java"), build_dto_response(module_pkg, entity, fields))

    # 2) Entity
    write_file(os.path.join(module_path, f"{entity}.java"), build_entity(module_pkg, entity, table, fields, relations))

    # 3) Repository
    write_file(os.path.join(module_path, f"{entity}Repository.java"), build_repository(module_pkg, entity))

    # 4) MapStruct mapper
    write_file(os.path.join(module_path, f"{entity}Mapper.java"), build_mapstruct_mapper(module_pkg, entity, fields))

    # 5) Service
    write_file(os.path.join(module_path, f"{entity}Service.java"), build_service(module_pkg, entity))

    # 6) Controller
    write_file(os.path.join(module_path, f"{entity}Controller.java"), build_controller(module_pkg, entity))

    print(f"✅ Module '{module}' generated at {module_path}")
    print("Generated files:")
    print(" -", os.path.join(module_path, f"{entity}.java"))
    print(" -", os.path.join(module_path, f"{entity}Repository.java"))
    print(" -", os.path.join(module_path, f"{entity}Mapper.java"))
    print(" -", os.path.join(module_path, f"{entity}Service.java"))
    print(" -", os.path.join(module_path, f"{entity}Controller.java"))
    print(" - dto:", os.path.join(dto_path, f"{entity}RequestDTO.java"))
    print(" - dto:", os.path.join(dto_path, f"{entity}ResponseDTO.java"))

if __name__ == "__main__":
    main()
