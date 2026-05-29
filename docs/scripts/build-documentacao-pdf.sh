#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! command -v typst >/dev/null 2>&1; then
  echo "Typst não encontrado. Instale com: brew install typst" >&2
  exit 1
fi

typst compile documentacao-tecnica.typ documentacao-tecnica-central-lj.pdf

echo "PDF gerado: ${ROOT}/documentacao-tecnica-central-lj.pdf"
