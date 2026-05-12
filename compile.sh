#!/bin/bash
# Compila todos os arquivos .java do projeto
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt
echo "Compilacao concluida. Execute: ./run.sh"
