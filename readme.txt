Command to Connect to Instance:
ssh -i "aws_final.pem" ubuntu@ec2-54-242-250-93.compute-1.amazonaws.com

The instance has one project directory Lucene_Cranfield_IR_19309019
Now perform the following operations:
cd Lucene_Cranfield_IR_19309019
mvn clean
mvn install
mvn exec:java -Dexec.mainClass="Indexfiles" -Dexec.args="-docs cran/cran.all.1400 -queries cran/cran.qry"

It will show the foolowing commands:
Indexing to directory 'index'....
Indexing documents.
Reading queries and creating search results.

This builds the project and produces ouput cran-results.txt in the project root folder.

Copy the results file to trec_eval foler:
cp cran-results.txt ./trec_eval_latest

go to trec_eval_latest folder
cd trec_eval_latest

Run the trec_eval
./trec_eval QRelsCorrectedforTRECeval cran-results.txt
This will give output scores in the screen

Src/main has all the source files