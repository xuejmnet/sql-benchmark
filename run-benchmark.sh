#!/bin/bash

# Easy-Query Benchmark Runner Script

echo "======================================"
echo "Easy-Query vs JOOQ Benchmark Suite"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if Maven is installed
if ! command -v mvn &> /dev/null
then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Build the project
echo -e "${BLUE}[1/3] Building project...${NC}"
mvn clean package -q

if [ $? -ne 0 ]; then
    echo "Build failed. Please check the errors above."
    exit 1
fi

echo -e "${GREEN}✓ Build successful${NC}"
echo ""

# Run benchmarks
echo -e "${BLUE}[2/3] Running benchmarks...${NC}"
echo "This may take several minutes..."
echo ""

# Create results directory if not exists
mkdir -p results

# Run all benchmarks and save results
java -jar target/benchmarks.jar -rf json -rff results/benchmark-results.json

if [ $? -ne 0 ]; then
    echo "Benchmark execution failed."
    exit 1
fi

echo ""
echo -e "${GREEN}✓ Benchmarks completed${NC}"
echo ""

# Display results summary
echo -e "${BLUE}[3/3] Results saved to:${NC}"
echo "  - results/benchmark-results.json"
echo ""
echo "To view detailed results:"
echo "  1. Visit http://jmh.morethan.io/"
echo "  2. Upload results/benchmark-results.json"
echo ""
echo -e "${GREEN}Done!${NC}"



