#!/bin/bash

REPORT_DIR="tests/report"
RESULTS_FILE="tests/results.jtl"
TEST_PLAN="tests/test_plan.jmx"

show_help() {
  echo "Uso: $0 [OPCIONES] [PARAMETROS_ADICIONALES]"
  echo "Ejecuta pruebas de carga con JMeter"
  echo
  echo "Opciones:"
  echo "  -t, --threads N    Número de usuarios concurrentes (default: 1)"
  echo "  -r, --rampup S     Tiempo de ramp-up en segundos (default: 1)"
  echo "  -d, --duration S   Duración de la prueba en segundos (default: null)"
  echo "  -h, --help         Muestra este mensaje"
  echo
  echo "Puedes pasar cualquier parámetro extra de JMeter, por ejemplo:"
  echo "  -Jserver=otrohost.com -Jport=443 -Jprotocol=https"
  echo
  echo "Ejemplos:"
  echo "  # Prueba básica con valores por defecto (1 usuario)"
  echo "  $0"
  echo
  echo "  # Prueba con 100 usuarios, ramp-up de 20s, duración de 5 minutos y servidor remoto"
  echo "  $0 -t 100 -r 20 -d 300 -Jserver=api.mi-servidor.com -Jport=443 -Jprotocol=https"
  exit 0
}

cleanup() {
  rm -rf "$REPORT_DIR"
  rm -f "$RESULTS_FILE"
}

# Procesar parámetros conocidos y dejar el resto para JMeter
JMETER_PARAMS=""
EXTRA_PARAMS=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    -t|--threads)
      JMETER_PARAMS+=" -Jthreads=$2"
      shift 2
      ;;
    -r|--rampup)
      JMETER_PARAMS+=" -Jrampup=$2"
      shift 2
      ;;
    -d|--duration)
      JMETER_PARAMS+=" -Jduration=$2"
      shift 2
      ;;
    -h|--help)
      show_help
      ;;
    *)
      EXTRA_PARAMS+=("$1")
      shift
      ;;
  esac
done

cleanup

echo "Iniciando prueba con:"
echo "Test Plan:    $TEST_PLAN"
echo "Reportes:     $REPORT_DIR"
echo "Parámetros:   $JMETER_PARAMS ${EXTRA_PARAMS[*]}"
echo

./bin/jmeter -n \
  -t "$TEST_PLAN" \
  -l "$RESULTS_FILE" \
  -e -o "$REPORT_DIR" \
  $JMETER_PARAMS "${EXTRA_PARAMS[@]}"

echo
echo "✅ Prueba completada. Reporte en: $REPORT_DIR"