
require(plyr)
require(ggplot2)
rm(list = ls())

 


f1<-read.csv(file="D:/Ethereum/experiments/alphacoralflows.txt1",sep="\t",header=F)
 
f1$myTime<-as.Date(strptime(paste(f1$V2, f1$V3,sep=" "), format="%Y %j"))
p1<-ggplot(data=f1,aes(x=myTime,y=V5/V6,colour=V1,linetype=V1))+geom_line()+scale_x_date() 
p1


f5<-read.csv(file="D:/Ethereum/experiments/alphacoralflows.txt5",sep="\t",header=F)

f5$myTime<-as.Date(strptime(paste(f5$V2, f5$V3,sep=" "), format="%Y %j"))
p2<-ggplot(data=f5,aes(x=myTime,y=V5/V6,colour=V1,linetype=V1))+geom_line()+scale_x_date() 
p2
 
f10<-read.csv(file="D:/Ethereum/experiments/alphacoralflows.txt10",sep="\t",header=F)

f10$myTime<-as.Date(strptime(paste(f10$V2, f10$V3,sep=" "), format="%Y %j"))
p3<-ggplot(data=f10,aes(x=myTime,y=V5/V6,colour=V1,linetype=V1))+geom_line()+scale_x_date() 
p3 

f1$motifSum<-rowSums(f1[,8:24])
f5$motifSum<-rowSums(f5[,8:24])
f10$motifSum<-rowSums(f10[,8:24])


m1<-cbind(k=rep(1,nrow(f1)),f1)
m5<-cbind(k=rep(5,nrow(f5)),f5)
m10<-cbind(k=rep(10,nrow(f10)),f10)
motifs<-rbind(m1,m5,m10)

motifs$V9<-motifs$V9/motifs$motifSum
motifs$V10<-motifs$V10/motifs$motifSum
motifs$V11<-motifs$V11/motifs$motifSum

adex<-motifs[motifs$V1=="aragon",]


write.csv(adex,file="D://aragonFlows")

ddply(motifs,.(token,Mytime,k), summarize, mean())
