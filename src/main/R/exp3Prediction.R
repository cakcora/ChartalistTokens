
library(ggplot2)
library(plyr)
library(rnn)
rm(list = ls())
lookback<-10
ahead =20

wDir<-"D:/Dropbox/Publications/PostDoc work/3 - Chartalist/"
pDir<-paste(wDir,"data/Priced/crawler/",sep="")
cleanName<-function(a){
  n<-gsub("TX.txt", "", a)
  n<-substring(n, 1+nchar("network"))
  return(n)
}

coreFile<-"results/Exp1Core/core.txt"
data <-(read.csv(paste(wDir,coreFile,sep=""),sep="\t",header=T))
data$token<-cleanName(data$token)
largeTokenTxCutoff<-50000
nidTokenTxCutoff<-10000

largeTokens<- ddply(data,.(token), summarize, txCount=sum(edges))
largeTokens<-largeTokens[largeTokens$txCount>largeTokenTxCutoff,]


midTokens<- ddply(data,.(token), summarize, txCount=sum(edges))
midTokens<-midTokens[midTokens$txCount>nidTokenTxCutoff,]

 
tokens<-largeTokens$token
tokens<-unique(data$token)
#tokens<-midTokens$token
#tokens<-as.data.frame(unique(data$token))
 
 


flows<-read.csv(file="D:/Ethereum/experiments/alphacoralflows.txt5",sep="\t",header=F)
flows$motifSum<-colSums(flows[,8:24])
colnames(flows)<-c("token","year","day","topvertices","allvertices","topedges","alledges","MNA","M003","M012","M102","M021D","M021U","M021C","M111D","M111U","M030T","M030C","M201","M120D","M120U","M120C","M210","M300","nullCell","ccbin0","ccbin1","ccbin2","ccbin3","ccbin4","ccbin5","ccbin6","ccbin7","ccbin8","ccbin9","ccbin10")
flows$myTime<-as.Date(strptime(paste(flows$year, flows$day,sep=" "), format="%Y %j"))
flows<-flows[(flows$token %in% tokens),]
da<-c()
for(t in tokens){
  toData<-flows[flows$V1==t,]
  da<-rbind(da,toData[1:lookback,])
}
p1<-ggplot(data=flows,aes(x=myTime,y=M021U,colour=token,linetype=token))+geom_line()+scale_x_date() 
p1
pr<-c()
success<-c()
for(t in tokens){
  tfile<-paste(pDir,t,sep="")
  if(file.exists(tfile)){
    message("data: ",t)
    prData<-read.csv(file=tfile,sep="\t",header=T)
   prData$Date <- as.Date(prData$Date,"%m/%d/%Y")
    prData$Price<-prData$Open/prData$Open[[1]]
  if(nrow(prData)>=ahead)
    success<-rbind(success,cbind(t,prData$Price[[ahead]]))
  pr<-rbind(pr,cbind(t,prData))
  }
  else{
    message("no data: ",t)
  }
}

success<-as.data.frame(success)
colnames(success)<-c("token","priceahead")
pr<-subset(pr, Market.Cap!="-")

##keras

read.table(wD)
