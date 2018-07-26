
require(plyr)
require(ggplot2)


#IGNORE THIS PART UNTIl BELOW
rm(list = ls())
cleanName<-function(a){
  n<-gsub("TX.txt", "", a)
  n<-substring(n, 1+nchar("network"))
  return(n)
}
if(FALSE){
comb<- function(x,y){
  return (choose(x,y))
}
MBDMy<- function(x)
{
  n <- nrow(x); d <- ncol(x) # n: number of observations (samples);  d: dimension of the data
    
    ## depth computation
    if (ncol(x) == 1) {x = t(x)}
    depth = rep(0,n)
    orderedMatrix = x
    if (n>1) {
      for (columns in 1:d) {
        orderedMatrix[,columns] <- sort(x[,columns])
        for (element in 1:n) {
          index1 <- length(which(orderedMatrix[,columns] < x[element,columns]))
          index2 <- length(which(orderedMatrix[,columns] <= x[element,columns]))
          multiplicity <- index2 - index1
          depth[element] <- depth[element] + index1 * (n - (index2)) + multiplicity * (n - index2 + index1) + comb(multiplicity,2)
        }   ### end FOR element
      }  ### end FOR columns
      depth <- depth / (d * comb(n,2) )
    } ## end IF
    if (n==1) {deepest <- x; depth <- 0}
    ordering<-order(depth,decreasing=TRUE)
  
  return(list(ordering=ordering,MBD=depth))
}
x<-matrix(c(1,3,3,3 ,8,8,8,8,3,7,4,4),nrow=4)
#new double[][]{{1,8,3},{3,8,7},{3,8,04},{3,8,04}};
MBDMy(x)
}
##################END OF THE IGNORED SECTION

smoother<-function(a){
  b<-c(a[1],a[2])
  for(i in 3:length(a)){
    b[i]=(a[i]+a[i-1]+a[i-2])/3
  }
  return(b)
}
adex<-read.csv(file="D:/Ethereum/alphacore/adex",sep="\t",header=T)
colnames(adex)<-c("token","node",	"coreVal")
summary(adex)
hist(adex$coreVal) 

d<-c()
wDir<-"D:/Dropbox/Publications/PostDoc work/3 - Chartalist/"
largeTokenTxCutoff<-50000
coreFile<-"results/Exp1Core/core.txt"

data <-(read.csv(paste(wDir,coreFile,sep=""),sep="\t",header=T))
data$myTime<-as.Date(strptime(paste(data$year, data$period,sep=" "), format="%Y %j"))
data$token<-cleanName(data$token)
largeTokens<- ddply(data,.(token), summarize, txCount=sum(edges),myTime=min(myTime))
largeTokens<-largeTokens[largeTokens$txCount>largeTokenTxCutoff,]
 


getTokenData<-function(tokens){
d<-c()
 highest<-100
   for(t in tokens)
     {
   message("token:",t," tx count:",largeTokens[largeTokens$token==t,]$txCount)
    alphacore<-read.csv(file=paste("D:/Ethereum/alphacore/",t,sep=""),sep="\t",header=F)
    colnames(alphacore)<-c("token","node",	"coreVal")
    #hist(alphacore$coreVal,main=t,xlab="AlphaCore value",ylab="number of nodes")
    
    kcore<-read.csv(file=paste("D:/Ethereum/kcore/",t,sep=""),sep="\t",header=F)
    colnames(kcore)<-c("token","node",	"coreVal")
    #hist(kcore$coreVal,main=t,xlab="Kcore value",ylab="number of nodes")
    k2<-as.data.frame(cbind(t,unname(quantile(kcore$coreVal, seq(0,1, by=1.0/highest))),array(0:highest)))
    colnames(k2)<-c("token","k","q")
    rownames(k2)<-NULL
    
    a2<-as.data.frame(cbind(t,unname(quantile(alphacore$coreVal, seq(0,1, by=1.0/highest))),array(0:highest)))
    colnames(a2)<-c("token","alpha","q")
    rownames(a2)<-NULL
    d<-rbind(d,a2)
    
  
 }
d$q <- as.numeric(as.character(d$q))
d$alpha <- as.numeric(as.character(d$alpha))
 return(d)
}

tokens<-c("aeternity","bancor","aion")
d=getTokenData(tokens)
p1<-ggplot(data=d,aes(x=q,y=alpha,colour=token,linetype=token))+geom_point()+scale_x_continuous() 
p1

tokens<-c("bnb","bat","storj")
d=getTokenData(tokens)
p1<-ggplot(data=d,aes(x=q,y=alpha,colour=token,linetype=token))+geom_point()+scale_x_continuous()      
p1 

tokens<-c("bytom","eos")
d=getTokenData(tokens)
p1<-ggplot(data=d,aes(x=q,y=alpha,colour=token,linetype=token))+geom_point()+scale_x_continuous()      
p1
 
 
d=getTokenData(largeTokens[15:20,]$token)
p1<-ggplot(data=d,aes(x=q,y=alpha,colour=token,linetype=token))+geom_point()+scale_x_continuous()      
p1
tokens<-c("bancor","bat","bnb")
flows<-read.csv(file="D:/Ethereum/experiments/alphacoralflows.txt",sep="\t",header=F)
flows<-flows[(flows$V1 %in% tokens),]
flows$myTime<-as.Date(strptime(paste(flows$V3, flows$V4,sep=" "), format="%Y %j"))
p1<-ggplot(data=flows,aes(x=myTime,y=V7/max(V7),colour=V1,linetype=V1))+geom_line()+scale_x_date() 
p1
