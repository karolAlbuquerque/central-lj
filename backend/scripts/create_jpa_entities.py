#!/usr/bin/env python3
"""Create JPA entities from current domain and rewrite domain as pure POJOs."""
from pathlib import Path
import re

SRC = Path(__file__).resolve().parents[1] / "src/main/java/br/edu/central/centrallj"
DOMAIN = SRC / "domain"
ENTITY = SRC / "adapter/out/persistence/entity"

ENTITY_MAP = {
    "Mission": "MissionEntity",
    "MissionHistory": "MissionHistoryEntity",
    "Heroi": "HeroiEntity",
    "EquipeHeroica": "EquipeHeroicaEntity",
    "Usuario": "UsuarioEntity",
}

JPA_ENTITIES = ["Mission", "MissionHistory", "Heroi", "EquipeHeroica", "Usuario"]


def to_entity(java: str, class_name: str, entity_name: str) -> str:
    out = java
    out = re.sub(r"^package\s+[\w.]+;", "package br.edu.central.centrallj.adapter.out.persistence.entity;", out, count=1)
    out = out.replace(f"class {class_name}", f"class {entity_name}")
    for old, new in ENTITY_MAP.items():
        out = re.sub(rf"\b{old}\b", new, out)
    return out


def to_pure_domain(java: str, class_name: str) -> str:
    out = java
    out = re.sub(r"^package\s+[\w.]+;", "package br.edu.central.centrallj.domain;", out, count=1)
    # remove jakarta imports and annotations
    lines = []
    for line in out.splitlines():
        if "jakarta.persistence" in line:
            continue
        if line.strip().startswith("@"):
            continue
        lines.append(line)
    out = "\n".join(lines)
    if class_name == "MissionHistory":
        # replace Mission mission field with UUID missionId
        out = re.sub(
            r"private Mission mission;",
            "private java.util.UUID missionId;",
            out,
        )
        out = re.sub(
            r"public Mission getMission\(\) \{\s*return mission;\s*\}",
            "public java.util.UUID getMissionId() { return missionId; }",
            out,
        )
        out = re.sub(
            r"public void setMission\(Mission mission\) \{\s*this\.mission = mission;\s*\}",
            "public void setMissionId(java.util.UUID missionId) { this.missionId = missionId; }",
            out,
        )
    return out


def main() -> None:
    ENTITY.mkdir(parents=True, exist_ok=True)
    for name in JPA_ENTITIES:
        path = DOMAIN / f"{name}.java"
        if not path.exists():
            continue
        raw = path.read_text(encoding="utf-8")
        entity_name = ENTITY_MAP[name]
        (ENTITY / f"{entity_name}.java").write_text(to_entity(raw, name, entity_name), encoding="utf-8")
        path.write_text(to_pure_domain(raw, name), encoding="utf-8")
    print("Entities and pure domain written.")


if __name__ == "__main__":
    main()
