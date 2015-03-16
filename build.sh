
set -x

rm *.jar
javac  main.java || exit
jar cfe main.jar main  *.class || exit
# rm *.class
java -jar main.jar


