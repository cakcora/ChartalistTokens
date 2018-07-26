require(plyr)
require(ggplot2)
library(ape)
rm(list = ls())

printPlots<-FALSE
wDir<-"D:/Dropbox/Publications/PostDoc work/3 - Chartalist/"
#C:\Users\cxa123230\Dropbox\Publications\PostDoc work\3 - Chartalist\results\Exp1Core
pDir<-paste(wDir,"/figures/",sep="")
myFontSize<-16
minDay<-30
minSellCount<-300
largeTokenTxCutoff<-50000
startDate<-as.Date('2017-06-01')
investorFile<-"results/Exp1Core/investorBehavior.txt"
coreFile<-"results/Exp1Core/core.txt"

cleanName<-function(a){
  n<-gsub("TX.txt", "", a)
  n<-substring(n, 1+nchar("network"))
  return(n)
}

########################################################################
#Experiments to determine holding times
dataInv <-(read.csv(paste(wDir,investorFile,sep=""),sep="\t",header=T))
dataInv$token<-cleanName(dataInv$token)
excluded<-c("beautychain1")
dataInv<-dataInv[!(dataInv$token %in% excluded),]
dataInv<-dataInv[dataInv$n>minSellCount,]
histCutoff<-50
plot1<-ggplot(dataInv,aes(meanselltime)) +
  geom_histogram(breaks=seq(0, histCutoff, by = 1), col="red", fill="red", alpha = .2) + 
  labs(title="") +
  labs(x="Average holding time", y="Token Count")+
  theme_minimal()+
  theme(text = element_text(size=myFontSize-2))
plot1

message( nrow(dataInv[dataInv$meanselltime>histCutoff,]), " tokens beyond ",histCutoff," days" )
data <-(read.csv(paste(wDir,coreFile,sep=""),sep="\t",header=T))
data$myTime<-as.Date(strptime(paste(data$year, data$period,sep=" "), format="%Y %j"))
data$token<-cleanName(data$token)
data<-data[data$myTime!=max(data$myTime),]

largeTokens<- ddply(data,.(token), summarize, txCount=sum(edges),myTime=min(myTime))
largeTokens<-largeTokens[largeTokens$txCount>largeTokenTxCutoff,]

dataInvSmall<- dataInv[!(dataInv$token %in% largeTokens$token), ]
 

dataInvLarge<-dataInv[dataInv$token %in% largeTokens$token, ]
 

if(printPlots){
ggsave(plot=plot1,file="exp1InvestorHoldingDist.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot1,file="exp1InvestorHoldingDist.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}
plot2<-ggplot(dataInvLarge,aes(meanselltime)) +
  geom_histogram(breaks=seq(0, histCutoff, by = 1), col="red", fill="red", alpha = .2) + 
  labs(title="") +
  labs(x="Average holding time", y="Token Count")+
  theme_minimal()+
  theme(text = element_text(size=myFontSize-2))
plot2
message( nrow(dataInvLarge[dataInvLarge$meanselltime>histCutoff,]), " tokens beyond ",histCutoff," days" )

if(printPlots){
ggsave(plot=plot2,file="exp1InvestorHoldingDistLarge.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot2,file="exp1InvestorHoldingDistLarge.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}

dataInvSmall<-na.omit(dataInvSmall)
dataInvSmall$Nt0<-dataInvSmall$t0/dataInvSmall$n
dataInvSmall$Nt1<-dataInvSmall$t1/dataInvSmall$n
dataInvSmall$Nt3<-dataInvSmall$t3/dataInvSmall$n
dataInvSmall$Nt7<-dataInvSmall$t7/dataInvSmall$n
dataInvSmall$Nt15<-dataInvSmall$t15/dataInvSmall$n
dataInvSmall$Nt30<-dataInvSmall$t30/dataInvSmall$n
dataInvSmall$Nt60<-dataInvSmall$t60/dataInvSmall$n

tVals<-c(median(dataInvSmall$Nt0),median(dataInvSmall$Nt1),median(dataInvSmall$Nt3),
         median(dataInvSmall$Nt7),median(dataInvSmall$Nt15),median(dataInvSmall$Nt30),median(dataInvSmall$Nt60))

dateArr<-c(0,1,3,7,15,30,60)
 
dataInvLarge<-na.omit(dataInvLarge)
dataInvLarge$Nt0<-dataInvLarge$t0/dataInvLarge$n
dataInvLarge$Nt1<-dataInvLarge$t1/dataInvLarge$n
dataInvLarge$Nt3<-dataInvLarge$t3/dataInvLarge$n
dataInvLarge$Nt7<-dataInvLarge$t7/dataInvLarge$n
dataInvLarge$Nt15<-dataInvLarge$t15/dataInvLarge$n
dataInvLarge$Nt30<-dataInvLarge$t30/dataInvLarge$n
dataInvLarge$Nt60<-dataInvLarge$t60/dataInvLarge$n

tValsLarge<-c(median(dataInvLarge$Nt0),median(dataInvLarge$Nt1),median(dataInvLarge$Nt3),
         median(dataInvLarge$Nt7),median(dataInvLarge$Nt15),median(dataInvLarge$Nt30),median(dataInvLarge$Nt60))

dateArr<-c(0,1,3,7,15,30,60)
plot3<-ggplot()+geom_line(aes(x=dateArr,y=tValsLarge,color='Large tokens'))+
  geom_line(aes(x=dateArr,y=tVals,color='Small tokens'))+
  scale_x_continuous(breaks=dateArr,name="Hold duration (day)")+
 theme_minimal()+scale_y_continuous(name=("Percentage of first sells"))+
 theme(text = element_text(size=myFontSize-2),legend.position = c(0.6, 0.6),legend.text = element_text(size=myFontSize+2))+
 scale_colour_manual(name='', values=c('Large tokens'='blue','Small tokens'='red'))+
 guides(color = guide_legend(override.aes = list(size=5)))
plot3
message(nrow(dataInvSmall)," small tokens, ",nrow(dataInvLarge)," large tokens. Each token has at least ",minSellCount," days")
if(printPlots){
ggsave(plot=plot3,file="exp1InvestorHoldingDuration.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot3,file="exp1InvestorHoldingDuration.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}


data$myTime<-as.Date(strptime(paste(data$year, data$period,sep=" "), format="%Y %j"))
data<-data[data$myTime!=max(data$myTime),]

invIndices<-c()
for(x in 0:9){ 
  for(y in 0:9){ 
    invIndices<-c(invIndices,paste("m",x,y,sep=""))
  }
}

 
 
invClData<- (subset(dataInvLarge, select=invIndices))
invClData$m00<-NULL
#(Normalizer)invClData<-t(apply(invClDataRaw, 1, function(x)(x-min(x))/(max(x)-min(x))))
row.names(invClData) <- dataInvLarge$token # Declare column 1 as the row names


dist(as.matrix(invClData)) -> d  
hc <- hclust(d,method="ward.D2")#"ward.D", "ward.D2", "single", "complete", "average" (= UPGMA), "mcquitty" (= WPGMA), "median" (= WPGMC) or "centroid" (= UPGMC).
plot(hc)
 

 

#install.packages("dendextend")
#install.packages("circlize")
library(dendextend)
library(circlize)

# create a dendrogram
 
dend <- as.dendrogram(hc)

# modify the dendrogram to have some colors in the branches and labels
dend <- dend %>% 
  color_branches(k=64) %>% 
  color_labels(k=64)
if(printPlots){
pdf(paste(pDir,sep="","exp1investorclu.pdf"),width=12,height=12,paper='special') 
par(mar = rep(1,4))
circlize_dendrogram(dend, dend_track_height = 0.3,labels_track_height=0.6) 
dev.off() 
}

if(printPlots){
  png(paste(pDir,sep="","exp1investorclu.png"),width=12,height=12,units="in",res=1600) 
  par(mar = rep(1,4))
  circlize_dendrogram(dend, dend_track_height = 0.3,labels_track_height=0.6) 
  dev.off() 
}
 
 
invClData 
invClDataNormed<-t(apply(invClData, 1, function(x)(x)/(sum(x))))
summary(invClDataNormed[,1])

cSums<-c(0,1,sum(invClDataNormed[,0:1])/nrow(invClDataNormed))
for(ind in 2:length(invClDataNormed[1,])){
  cSums<-rbind(cSums, c(as.integer(ind/10),ind%%10,sum(colSums(invClDataNormed[,0:ind],dims=1))/nrow(invClDataNormed)))
}
cSums<-as.data.frame(cSums)
rownames(cSums)<-NULL
colnames(cSums)<-c("Buy","Sell","Perc")
summary(cSums)
cumBuys<- ddply(cSums,.(Buy), summarize, b=max(Perc))
cumSells<- ddply(cSums,.(Sell), summarize, b=max(Perc))
colnames(cumBuys)<-c("Buy","Perc")
colnames(cumSells)<-c("Sell","Perc")
message("Cumulative buying percentages")
cumBuys
plot5<-ggplot()+geom_line(aes(x=cumBuys$Buy,y=cumBuys$Perc))+
  #geom_line(aes(x=cumSells$Sell,y=cumSells$Perc))+
  scale_x_discrete(limits=c(0,1,2,3,4,5,6,7,8,9))+
  labs(title="") +
  labs(x="Number of buys", y="Percentage of all users")+
  theme_minimal()+
  theme(text = element_text(size=myFontSize))
plot5
if(printPlots){
  ggsave(plot=plot5,file="exp1Investorinvestorbuys.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
  ggsave(plot=plot5,file="exp1Investorinvestorbuys.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}
 
pSums<-c(0,1,sum(invClDataNormed[,0:1])/nrow(invClDataNormed))
for(ind in 2:length(invClDataNormed[1,])){
  pSums<-rbind(pSums, c(as.integer(ind/10),ind%%10,sum((invClDataNormed[,ind]))/nrow(invClDataNormed)))
}
pSums<-as.data.frame(pSums)
colnames(pSums)<-c("Buy","Sell","Perc")

plot7<-ggplot(pSums, aes(Buy, Sell)) +
  geom_tile(aes(fill = x<-sqrt(sqrt(100*Perc))) )+ 
  geom_text(aes(label = round(100*Perc, 1))) +
  scale_fill_gradient(low = "white", high = "red")+
  theme_minimal()+
  scale_x_discrete(limits=c(0,1,3,5,7,9))+
  scale_y_discrete(limits=c(0,1,3,5,7,9))+
  theme(legend.position = 'none')+
  theme(text = element_text(size=myFontSize-2),legend.text = element_text(size=myFontSize+2))
plot7
if(printPlots){
  ggsave(plot=plot7,file="exp1Investorinvestorheat.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
  ggsave(plot=plot7,file="exp1Investorinvestorheat.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}

#----------------------------------------

   
