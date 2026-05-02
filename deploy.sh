cp .env.example .env
set -a && source .env && set +a && mvn -pl JavaWallet compile exec:java