# Instructions to run the program

1. The jar file (`DrugInteraction-1-SNAPSHOT-jar-with-dependencies.jar`) is in `target` folder. This is a maven project
2. Run the jar file like this `java -jar DrugInteraction-1-SNAPSHOT-jar-with-dependencies.jar`
3. It is going to give below information and asks you to enter the drugs names on each line

```
 Read full data from json file with 152 interactions

 Start typing. Pressing Return key will consider as a new line. When you are done entering drugs data, press Return key twice.
```
4. Enter the drug names on each line. You can enter in multiple lines like this

```
sildenafil tamsulosin valaciclovir
sildenafil ibuprofen
valaciclovir doxepin ticlopidine ibuprofen
```
5. After you are done entering the drugs list just press double enter and it will process your input
6. If you have entered more than 20 drugs in a single sentence it will break from taking input and process already entered input (if any)
7. If you have entered more than 10000 lines, it will break from taking input and process already entered input
8. I have added commands easily to follow when you run the main function
9. I will attach a screenshot with my processed information and output

#### This program mainly solves these goals:
1. If there are multiple interactions detected for a single line of input, the program should return the most
severe interaction. 
2. If there are multiple interactions of the same severity, the program should return the
interaction that appears first in the interactions.json file.
3. The program should read its input from STDIN and write its output to STDOUT, where each line of input
should generate a line of output in the same order.

#### Efficiency
This is attempted in a kind of brute force manner as the number of entries are max 20c2 = (190) * 10000
I made it a little efficient by merging the strings when taking in the json file and also merging the input strings
So the string comparison makes it more efficient by bringing down the actual effort by half

There are couple of other ways to make it more efficient such as `caching` the data when we read, so that next read gets it easier etc.
Also the algorithm can be `divide-and-conquer` rather than go `top-down`
