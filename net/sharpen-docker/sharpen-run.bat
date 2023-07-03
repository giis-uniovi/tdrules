rem run from solution folder
docker build -t sharpen .\sharpen-docker && docker run -v %CD%:/sharpen/workdir sharpen . sharpen-temp/java @sharpen-all-options.txt