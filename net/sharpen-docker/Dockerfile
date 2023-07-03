#Builds a java 7 container with the sharpen jar that translates the code of the java project
#(java code translated is in sharpen-temp/java, previously preprocessed)

#Command to build and execute container 
#(run from the .net project that is translated, mapped to the container workdir)
# docker build -t sharpen .\sharpen-docker && docker run -v %CD%:/sharpen/workdir sharpen . sharpen-temp/java @sharpen-all-options.txt

FROM azul/zulu-openjdk-alpine:7u322
RUN mkdir /sharpen && cd /sharpen &&  wget -q https://github.com/javiertuya/sharpen/releases/download/v1.1.0-fork/sharpencore-1.1.0-fork-jar-with-dependencies.jar
COPY entrypoint.sh /sharpen/entrypoint.sh
RUN mkdir /sharpen/workdir
WORKDIR /sharpen/workdir
#ENTRYPOINT ["java", "-jar", "/sharpen/sharpencore-1.1.0-fork-jar-with-dependencies.jar", "sharpen-temp/java","@sharpen-all-options.txt"]
ENTRYPOINT ["sh","/sharpen/entrypoint.sh"]
