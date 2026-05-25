#!/usr/bin/env python3
"""One-shot migration to hexagonal package layout."""
from __future__ import annotations

import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/java/br/edu/central/centrallj"

MOVES: list[tuple[str, str]] = [
    ("controller", "adapter/in/web/controller"),
    ("dto", "adapter/in/web/dto"),
    ("repository", "adapter/out/persistence/repository"),
    ("messaging/consumer", "adapter/in/messaging"),
    ("messaging/ingestion", "adapter/in/messaging"),
    ("messaging/producer", "adapter/out/messaging/producer"),
    ("messaging/event", "adapter/out/messaging/event"),
    ("messaging/support", "application/support"),
    ("security", "adapter/in/web/security"),
    ("exception", "adapter/in/web/exception"),
]

SERVICE_FILES = [
    "MissionCommandService.java",
    "MissionQueryService.java",
    "MissionAssignmentService.java",
    "HeroiService.java",
    "EquipeHeroicaService.java",
    "AuthService.java",
    "MissionWorkflowService.java",
    "MissionHistoryRecorder.java",
    "EventPublishTestService.java",
    "MissionTestService.java",
]

WORKFLOW_FILES = [
    "MissionProcessingFlowStrategy.java",
    "DefaultMissionProcessingFlowStrategy.java",
    "CriticalPriorityMissionProcessingFlowStrategy.java",
    "MissionProcessingFlowStrategyResolver.java",
]

IMPORT_REPLACEMENTS = [
    (r"br\.edu\.central\.centrallj\.controller", "br.edu.central.centrallj.adapter.in.web.controller"),
    (r"br\.edu\.central\.centrallj\.dto", "br.edu.central.centrallj.adapter.in.web.dto"),
    (r"br\.edu\.central\.centrallj\.repository", "br.edu.central.centrallj.adapter.out.persistence.repository"),
    (r"br\.edu\.central\.centrallj\.messaging\.consumer", "br.edu.central.centrallj.adapter.in.messaging"),
    (r"br\.edu\.central\.centrallj\.messaging\.ingestion", "br.edu.central.centrallj.adapter.in.messaging"),
    (r"br\.edu\.central\.centrallj\.messaging\.producer", "br.edu.central.centrallj.adapter.out.messaging.producer"),
    (r"br\.edu\.central\.centrallj\.messaging\.event", "br.edu.central.centrallj.adapter.out.messaging.event"),
    (r"br\.edu\.central\.centrallj\.messaging\.support", "br.edu.central.centrallj.application.support"),
    (r"br\.edu\.central\.centrallj\.security", "br.edu.central.centrallj.adapter.in.web.security"),
    (r"br\.edu\.central\.centrallj\.exception", "br.edu.central.centrallj.adapter.in.web.exception"),
    (r"br\.edu\.central\.centrallj\.service\.workflow", "br.edu.central.centrallj.application.service.workflow"),
    (r"br\.edu\.central\.centrallj\.service\.Mission", "br.edu.central.centrallj.application.service.Mission"),
    (r"br\.edu\.central\.centrallj\.service", "br.edu.central.centrallj.application.service"),
]


def move_dir(src_rel: str, dst_rel: str) -> None:
    src = SRC / src_rel
    dst = SRC / dst_rel
    if not src.exists():
        return
    dst.parent.mkdir(parents=True, exist_ok=True)
    if dst.exists():
        shutil.rmtree(dst)
    shutil.move(str(src), str(dst))


def update_java_file(path: Path) -> None:
    text = path.read_text(encoding="utf-8")
    for old, new in IMPORT_REPLACEMENTS:
        text = re.sub(old, new, text)
    # package line from path
    rel = path.relative_to(SRC).with_suffix("")
    pkg = "br.edu.central.centrallj." + ".".join(rel.parts[:-1])
    text = re.sub(r"^package\s+[\w.]+;", f"package {pkg};", text, count=1)
    path.write_text(text, encoding="utf-8")


def move_services() -> None:
    svc_src = SRC / "service"
    if not svc_src.exists():
        return
    app_svc = SRC / "application/service"
    app_svc.mkdir(parents=True, exist_ok=True)
    wf_dst = app_svc / "workflow"
    wf_dst.mkdir(parents=True, exist_ok=True)

    for name in SERVICE_FILES:
        f = svc_src / name
        if f.exists():
            shutil.move(str(f), str(app_svc / name))

    wf_src = svc_src / "workflow"
    if wf_src.exists():
        for name in WORKFLOW_FILES:
            f = wf_src / name
            if f.exists():
                shutil.move(str(f), str(wf_dst / name))
        if wf_src.exists() and not any(wf_src.iterdir()):
            wf_src.rmdir()

    # MissionRealtimeNotifier -> adapter/out/realtime
    rt = SRC / "adapter/out/realtime"
    rt.mkdir(parents=True, exist_ok=True)
    notifier = svc_src / "MissionRealtimeNotifier.java"
    if notifier.exists():
        shutil.move(str(notifier), str(rt / "SseMissionNotificationAdapter.java"))
    if svc_src.exists() and not any(svc_src.rglob("*")):
        shutil.rmtree(svc_src)


def move_mappers() -> None:
    dto = SRC / "adapter/in/web/dto"
    mapper_dst = SRC / "adapter/in/web/mapper"
    mapper_dst.mkdir(parents=True, exist_ok=True)
    for name in ("MissionMapper.java", "HeroMapper.java", "EquipeMapper.java"):
        f = dto / name
        if f.exists():
            shutil.move(str(f), str(mapper_dst / name))


def fix_sse_adapter() -> None:
    path = SRC / "adapter/out/realtime/SseMissionNotificationAdapter.java"
    if not path.exists():
        return
    text = path.read_text(encoding="utf-8")
    text = text.replace("class MissionRealtimeNotifier", "class SseMissionNotificationAdapter")
    text = text.replace("MissionRealtimeNotifier(", "SseMissionNotificationAdapter(")
    text = re.sub(r"^package\s+[\w.]+;", "package br.edu.central.centrallj.adapter.out.realtime;", text, count=1)
    path.write_text(text, encoding="utf-8")


def walk_update() -> None:
    for java in SRC.rglob("*.java"):
        update_java_file(java)


def main() -> None:
    for src_rel, dst_rel in MOVES:
        move_dir(src_rel, dst_rel)
    move_services()
    move_mappers()
    fix_sse_adapter()
    walk_update()
    print("Package moves done.")


if __name__ == "__main__":
    main()
