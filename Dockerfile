# Imagem do Musichords para deploy na nuvem (Render, ou qualquer serviço que rode Docker).
#   docker build -t musichords .
#   docker run -p 8080:8080 -e PORT=8080 musichords   -> http://localhost:8080

# 1) Compila o .jar a partir do código-fonte, com os testes rodando antes
FROM eclipse-temurin:21-jdk AS build
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY Musichords/ ./
RUN sh testar.sh && sh compilar.sh

# 2) Imagem final, só com o Java para rodar (sem compilador) + o .jar e o grafo.txt
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/Musichords.jar /app/grafo.txt ./
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-jar", "Musichords.jar"]
