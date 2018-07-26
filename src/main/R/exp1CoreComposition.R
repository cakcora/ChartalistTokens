require(plyr)
require(ggplot2)

rm(list = ls())
wDir<-"D:/Dropbox/Publications/PostDoc work/3 - Chartalist/"
pDir<-paste(wDir,"figures/",sep="")
rDir<-paste(wDir,"results/Exp1Core/",sep="")
printPlots<-FALSE

startDate<-as.Date('2017-06-01')
coreFile<-"results/Exp1Core/core.txt"
priceFile<-"data/Priced/ETHER.txt"

data <-(read.csv(paste(wDir,coreFile,sep=""),sep="\t",header=T))
myFontSize<-16
minDay<-30
largeTokenTxCutoff<-50000

cleanName<-function(a){
  n<-gsub("TX.txt", "", a)
  n<-substring(n, 1+nchar("network"))
  return(n)
}
data$token<-cleanName(data$token)
smoother<-function(a){
  b<-c(a[1],a[2])
  for(i in 3:length(a)){
    b[i]=(a[i]+a[i-1]+a[i-2])/3
  }
  return(b)
}


data$myTime<-as.Date(strptime(paste(data$year, data$period,sep=" "), format="%Y %j"))
data<-data[data$myTime!=max(data$myTime),]


dat<- ddply(data[data$myTime>startDate,] ,.(myTime), summarize, new = sum(newnodes), 
            total=sum(nodes), edge=sum(edges),ret1=mean(retention1),ret3=mean(retention3),
            ret6=mean(retention6))
 
psize<-1
plot<-ggplot(data=dat,aes(x=myTime, y=smoother(edge),colour="Edge Count"))+geom_line(size=psize) 
plot<-plot+geom_line(aes(x=myTime, y=smoother(new),colour="New Users"),size=psize) 
plot<-plot+geom_line(aes(x=myTime, y=smoother(total),colour="All Users"),size=psize) 
plot<-plot+theme_minimal()+scale_x_date(name="Time")
plot<-plot+scale_y_continuous(name="Count")
plot<-plot+ scale_colour_manual(name='', values=c('All Users'='red', 'New Users'='blue', 'Edge Count'='green'))
plot<-plot+theme(text = element_text(size=myFontSize-2),legend.position = c(0.2, 0.6),legend.text = element_text(size=myFontSize))
plot<-plot+guides(color = guide_legend(override.aes = list(size=5)))
plot

summary(dat$new/dat$total)
auxPlot1<-ggplot(data=dat,aes(x=myTime, y=smoother(new/total)))+geom_line(size=psize)+
  theme_minimal()+scale_x_date(name="Time")+
  scale_y_continuous(name="Ratio of new investors")+
  theme(text = element_text(size=myFontSize-2))
auxPlot1

summary(dat$edge/dat$total)

auxPlot2<-ggplot(data=dat,aes(x=myTime, y=smoother(edge/total)))+geom_line(size=psize)+
  theme_minimal()+scale_x_date(name="Time")+
  scale_y_continuous(name="Ratio of Edges/Nodes")+
  theme(text = element_text(size=myFontSize-2))
auxPlot2

if(printPlots){
ggsave(plot=plot,file="exp1users.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot,file="exp1users.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)

ggsave(plot=plot,file="exp1newusers.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot,file="exp1newusers.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)

ggsave(plot=plot,file="exp1EdgesTotal.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot,file="exp1EdgesTotal.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}
priceData <-(read.csv(paste(wDir,priceFile,sep=""),sep="\t",header=T))
priceData$Date<-as.Date(priceData$Date, "%m/%d/%Y")
priceData<-priceData[priceData$Date>=min(dat$myTime)&priceData$Date<=max(dat$myTime),]
priceData$Close<-priceData$Close/max(priceData$Close)

plot6<-ggplot()
plot6<-plot6+geom_line(aes(x=priceData$Date,y=priceData$Close,colour="Price"))
total2<-smoother(dat$total)/max(smoother(dat$total))
new2<-smoother(dat$new)/max(smoother(dat$new))
plot6<-plot6+geom_line(aes(x=dat$myTime,y=total2,colour="Market Participants"))
plot6<-plot6+geom_line(aes(x=dat$myTime,y=new2,colour="New Buyers"))
plot6<-plot6+theme_minimal()+scale_x_date(name="Time")+scale_y_continuous(name="Ratio")
plot6<-plot6+scale_colour_manual(name='', values=c('Price'='red', 'New Buyers'='blue','Market Participants'='green'))
plot6<-plot6+theme(text = element_text(size=myFontSize-2),legend.position = c(0.2, 0.8),legend.text = element_text(size=myFontSize))
plot6<-plot6+guides(color = guide_legend(override.aes = list(size=5)))
plot6
if(printPlots){
ggsave(plot=plot6,file="exp1newusersonprice.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot6,file="exp1newusersonprice.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}

##-------------------------------------------------------------------------------------------------
data$coreSum<-data$c1+data$c2+data$c3+data$c4+data$c5+data$c6+data$c7+data$c8+data$c9+data$c10


largeTokens<- ddply(data,.(token), summarize, txCount=sum(edges))
largeTokens<-largeTokens[largeTokens$txCount>largeTokenTxCutoff,]

dataLarge<-data[data$token %in% largeTokens$token, ]
dataLarge<-dataLarge[dataLarge$myTime>startDate,]

dataSmall<- data[!(data$token %in% largeTokens$token), ]
dataSmall<-dataSmall[dataSmall$myTime>startDate,]

retValuesOnLarge<- ddply(dataLarge,.(myTime), summarize, ret1 = mean(retention1),  
                  ret3=mean(retention3), ret6=mean(retention6))
plot2<-ggplot(data=retValuesOnLarge)+geom_line(aes(x=myTime, y=smoother(smoother(ret1)),colour="All"),size=psize) 
plot2<-plot2+geom_line(aes(x=myTime, y=smoother(smoother(ret3)),colour="Core 3 and higher"),size=psize)  
plot2<-plot2+geom_line(aes(x=myTime, y=smoother(smoother(ret6)),colour="Core 6 and higher"),size=psize) 
plot2
plot2<-plot2+theme_minimal()+scale_x_date(name="Time")+scale_y_continuous(name="Ratio")
plot2<-plot2+  scale_colour_manual(name='', values=c('All'='red', 'Core 3 and higher'='blue', 'Core 6 and higher'='green'))
plot2<-plot2+theme(text = element_text(size=myFontSize-2),legend.position = c(0.4, 0.85),legend.text = element_text(size=myFontSize))
plot2<-plot2+guides(color = guide_legend(override.aes = list(size=5)))
plot2
if(printPlots){
ggsave(plot=plot2,file="exp1core.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot2,file="exp1core.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}

 

coresOnLarge<- ddply(dataLarge,.(myTime), summarize, deg = mean(degeneracy),  
              c1=mean(c1/coreSum),c2=mean(c2/coreSum),c3=mean(c3/coreSum),
              c4=mean(c4/coreSum),c5=mean(c5/coreSum),c6=mean(c6/coreSum),
              c7=mean(c7/coreSum),c8=mean(c8/coreSum),
              c9=mean(c9/coreSum),c10=mean(c10/coreSum),txCount=sum(coreSum)) 
 
plot3<-ggplot(data=coresOnLarge)+geom_line(aes(x=myTime,y=((c10)),colour="Core 10"))
plot3<-plot3+geom_line(aes(x=myTime,y=((c9)),colour="Core 9"))
#plot3<-plot3+geom_line(aes(x=myTime,y=c8,colour="Core 8"))
plot3<-plot3+theme_minimal()+scale_x_date(name="Time")+scale_y_log10(name="Ratio")
plot3<-plot3+scale_colour_manual(name='', values=c('Core 8'='black', 'Core 9'='red', 'Core 10'='blue'))
plot3<-plot3+theme(text = element_text(size=myFontSize-2),legend.position = c(0.7, 0.8),legend.text = element_text(size=myFontSize))
plot3<-plot3+guides(color = guide_legend(override.aes = list(size=5)))
plot3
if(printPlots){
ggsave(plot=plot3,file="exp1core910.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot3,file="exp1core910.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}
plot4<-ggplot(data=coresOnLarge)+geom_line(aes(x=myTime,y=c1,colour="Core 1"))
plot4<-plot4+geom_line(aes(x=myTime,y=c2,colour="Core 2"))
plot4<-plot4+geom_line(aes(x=myTime,y=c3,colour="Core 3"))
plot4<-plot4+theme_minimal()+scale_x_date(name="Time")+scale_y_log10(name="Ratio")
plot4<-plot4+scale_colour_manual(name='', values=c('Core 1'='red', 'Core 2'='black', 'Core 3'='blue'))
plot4<-plot4+theme(text = element_text(size=myFontSize-2),legend.position = c(0.8, 0.3),legend.text = element_text(size=myFontSize))
plot4<-plot4+guides(color = guide_legend(override.aes = list(size=5)))
plot4
if(printPlots){
ggsave(plot=plot4,file="exp1core123.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot4,file="exp1core123.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}

plot5<-ggplot(data=coresOnLarge,aes(x=myTime,y=smoother(deg)))+geom_line(colour="blue")
plot5<-plot5+theme_minimal()+scale_x_date(name="Time")+scale_y_continuous(name="Degeneracy")
plot5<-plot5+theme(text = element_text(size=myFontSize-2),legend.text = element_text(size=myFontSize))
plot5
if(printPlots){
ggsave(plot=plot5,file="exp1degeneracy.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot5,file="exp1degeneracy.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}
#-------------------------------------------------------------------------------------
#Individual behavior plots
#-------------------------------------------------------------------------------------
indLarge<- ddply(dataLarge,.(token,myTime), summarize, deg = median(degeneracy),  
                     c1=mean(c1/coreSum),c2=mean(c2/coreSum),c3=mean(c3/coreSum),
                     c4=mean(c4/coreSum),c5=mean(c5/coreSum),c6=mean(c6/coreSum),
                     c7=mean(c7/coreSum),c8=mean(c8/coreSum),
                     c9=mean(c9/coreSum),c10=mean(c10/coreSum),txCount=sum(coreSum)) 
 

plot7<-ggplot()+geom_line(aes(x=indLarge[indLarge$token=="eos",]$myTime,y=smoother(indLarge[indLarge$token=="eos",]$deg),color="1-EOS"))
plot7<-plot7+geom_line(aes(x=indLarge[indLarge$token=="tronix",]$myTime,y=smoother(indLarge[indLarge$token=="tronix",]$deg),color="2-Tronix"))
plot7<-plot7+geom_line(aes(x=indLarge[indLarge$token=="bnb",]$myTime,y=smoother(indLarge[indLarge$token=="bnb",]$deg),color="3-BNB"))
plot7<-plot7+geom_line(aes(x=indLarge[indLarge$token=="vechain",]$myTime,y=smoother(indLarge[indLarge$token=="vechain",]$deg),color="4-VeChain"))
plot7<-plot7+scale_colour_manual(name='', values=c('1-EOS'='blue', '2-Tronix'='gray47','3-BNB'='green','4-VeChain'='red'))
plot7<-plot7+theme_minimal()+scale_x_date(name="Time")+scale_y_sqrt(name="Degeneracy")
plot7<-plot7+theme(text = element_text(size=myFontSize-2),legend.position = c(0.2, 0.8),legend.text = element_text(size=myFontSize+2))
plot7<-plot7+guides(color = guide_legend(override.aes = list(size=5)))
plot7
date1<-as.Date('2018-01-01')
ilAux<-indLarge[indLarge$myTime>date1,]
plot7aux<-ggplot()+geom_line(aes(x=ilAux[ilAux$token=="eos",]$myTime,y=smoother(ilAux[ilAux$token=="eos",]$deg),color="1-EOS"))+
 geom_line(aes(x=ilAux[ilAux$token=="tronix",]$myTime,y=smoother(ilAux[ilAux$token=="tronix",]$deg),color="2-Tronix"))+
 geom_line(aes(x=ilAux[ilAux$token=="bnb",]$myTime,y=smoother(ilAux[ilAux$token=="bnb",]$deg),color="3-BNB"))+
 geom_line(aes(x=ilAux[ilAux$token=="vechain",]$myTime,y=smoother(ilAux[ilAux$token=="vechain",]$deg),color="4-VeChain"))+
 scale_colour_manual(name='', values=c('1-EOS'='blue', '2-Tronix'='gray47','3-BNB'='green','4-VeChain'='red'))+
 theme_minimal()+scale_x_date(name="2018")+scale_y_continuous(name="Degeneracy")+
 theme(text = element_text(size=myFontSize-2),legend.position = c(0.2, 0.8),legend.text = element_text(size=myFontSize+2))+
 guides(color = guide_legend(override.aes = list(size=5)))
 plot7aux
if(printPlots){
ggsave(plot=plot7,file="exp1degeneracyLarge.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot7,file="exp1degeneracyLarge.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)

ggsave(plot=plot7aux,file="exp1degeneracyLargeAux.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot7aux,file="exp1degeneracyLargeAux.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)

}
library(depthTools)

#1 - depth of days
ind<-indLarge[indLarge$myTime>as.Date('2017-09-01'),]
chooseToken<-function(x){
  return (subset(ind[ind$token==x,],select=c("myTime","deg")))
}
arr<-c()
for(token in largeTokens$token){
  
  x<-(chooseToken(token))
  if(min(x$myTime)<as.Date('2017-09-03')){
    arr<-cbind(arr,x$deg/max(x$deg))
    
  }   
}

tokenTimes<- ddply(dataLarge,.(token), summarize, minT = min(myTime)) 
#View(tokenTimes)
depth<-c(c(MBD(arr,ylab='y',xlab='c'))$MBD)
deVal<-as.data.frame(cbind(depth,myTime=x$myTime))

deVal$myTime<-x$myTime 
plot8<-ggplot(data=(deVal))+geom_line(aes(x=myTime,y=smoother(depth)))+
 theme_minimal()+scale_x_date(name=paste("Time with data from ",ncol(arr),"largest tokens") )+
  scale_y_continuous(name=("Depth in degeneracy"))+
  theme(text = element_text(size=myFontSize-2),legend.position = c(0.2, 0.8),legend.text = element_text(size=myFontSize+2))+
  scale_colour_manual(name='', values=c('1'='blue'))

plot8
if(printPlots){
ggsave(plot=plot8,file="exp1daydepth.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot8,file="exp1daydepth.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}
#2 - cluster of tokens with degeneracy
arr2<-c()
nArr<-c()
for(token in largeTokens$token){
  
  x<-(chooseToken(token))
  if(min(x$myTime)<as.Date('2017-09-03')){
    arr2<-rbind(arr2,x$deg/max(x$deg))
    nArr<-rbind(nArr,token)
    
  }   
}

 
row.names(arr2) <- nArr # Declare column 1 as the row names
dist(as.matrix(arr2)) -> d  
hc <- hclust(d)
if(printPlots){
png(paste(pDir,'exp1tokenclu.png',sep=""),width = 10, height = 6, units = "in",res=1600)
}
plot(hc)
if(printPlots){
dev.off()
}
if(printPlots){
pdf(paste(pDir,'exp1tokenclu.pdf',sep=""),paper='special',width=10,height=6)
}
plot(hc)
if(printPlots){
dev.off()
}
###################################################################
#Individual token behavior in terms of retention. What are the best tokens?
require(reshape)

#1 - retention in large tokens
message("We are using tokens that have a minimum of ",minDay," day history.")
retValuesLarge<- ddply(dataLarge,.(token), summarize, All = median(retention1),  
                         core3=median(retention3), core6=median(retention6),N=length(retention6))
r1<-retValuesLarge[retValuesLarge$N>minDay,]
r1$N<-NULL
mdata <- melt(r1)

plot10 <- ggplot(mdata, aes(y=value,x=variable,fill=variable)) + geom_boxplot(outlier.colour="red", outlier.shape=16,
                                                                  outlier.size=2, notch=FALSE)+  labs(title="",x="Core Retention", y = "Perc.")
plot10<-plot10 + scale_fill_brewer(palette="Blues") +   theme_minimal() + theme(legend.position='none',text = element_text(size=myFontSize-2))
plot10

#2 - retention in small tokens

retValues<- ddply(dataSmall,.(token), summarize, All = median(retention1),  
                  core3=median(retention3), core6=median(retention6),N=length(retention6))
r1Small<-retValues[retValues$N>minDay,]
message("We are using tokens that have a minimum of ",minDay," day history.")
r1Small$N<-NULL
mdataSmall <- melt(r1Small)
plot11 <- ggplot(mdataSmall, aes(y=value,x=variable,fill=variable)) + geom_boxplot(outlier.colour="red", outlier.shape=16,
                                                                              outlier.size=2, notch=FALSE)+  labs(title="",x="Core Retention", y = "Perc.")
plot11<-plot11 + scale_fill_brewer(palette="Blues") +   theme_minimal() + theme(legend.position='none',text = element_text(size=myFontSize-2))
plot11
if(printPlots){
ggsave(plot=plot10,file="exp1coreretentionlarge.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot10,file="exp1coreretentionlarge.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}
if(printPlots){
ggsave(plot=plot11,file="exp1coreretentionsmall.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot11,file="exp1coreretentionsmall.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}
###########################################################################
#core distribution among tokens.

coreDistOnLarge<- ddply(dataLarge,.(token), summarize,  
                     c2=mean(c2/coreSum),c3=mean(c3/coreSum),
                     c4=mean(c4/coreSum),c5=mean(c5/coreSum),c6=mean(c6/coreSum),
                     c7=mean(c7/coreSum),c8=mean(c8/coreSum),
                     c9=mean(c9/coreSum),c10=mean(c10/coreSum),N=length(degeneracy)) 

rLarge<-coreDistOnLarge[coreDistOnLarge$N>minDay,]
 
t10<-coreDistOnLarge[coreDistOnLarge$c10>mean(coreDistOnLarge$c10)+1.5*sd(coreDistOnLarge$c10),]$token
t10
 
t9<-coreDistOnLarge[coreDistOnLarge$c9>mean(coreDistOnLarge$c9)+1.5*sd(coreDistOnLarge$c9),]$token
t9
 
t8<-coreDistOnLarge[coreDistOnLarge$c8>mean(coreDistOnLarge$c8)+1.5*sd(coreDistOnLarge$c8),]$token
t8
intersect(t10,intersect(t9,t8))

rLarge$N<-NULL
mdata3 <- melt(rLarge)
if(printPlots) write.table(as.data.frame(rLarge), file = paste(rDir,"retentionlarge.csv",sep=""),sep = "\t" ,row.names = FALSE)

plot12 <- ggplot(mdata3, aes(y=value,x=variable,fill=variable)) +
 geom_boxplot(outlier.colour="red", outlier.shape=16,outlier.size=2, notch=FALSE)+
 labs(title="",x="Core Distribution in large tokens", y = "Perc.")+
 scale_fill_brewer(palette="Blues") +   theme_minimal() +
 theme(legend.position='none',text = element_text(size=myFontSize-2))
 
plot12

if(printPlots){
  ggsave(plot=plot12,file="exp1coreboxplotLarge.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
  ggsave(plot=plot12,file="exp1coreboxplotLarge.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}

coreDistSm<- ddply(dataSmall,.(token), summarize,  
                        c2=mean(c2/coreSum),c3=mean(c3/coreSum),
                        c4=mean(c4/coreSum),c5=mean(c5/coreSum),c6=mean(c6/coreSum),
                        c7=mean(c7/coreSum),c8=mean(c8/coreSum),
                        c9=mean(c9/coreSum),c10=mean(c10/coreSum),
                    N=length(degeneracy)) 
promising<-cbind(coreDistSm,promise=(4*coreDistSm$c4+5*coreDistSm$c5+6*coreDistSm$c6+7*coreDistSm$c7+8*coreDistSm$c8+9*coreDistSm$c9+10*coreDistSm$c10))
promising<-promising[promising$N>150,]
message(nrow(promising)," promising tokens exist.")
if(printPlots) write.table(as.data.frame(promising), file = paste(rDir,"promising.csv",sep=""),sep = "\t" ,row.names = FALSE)
rSmall<-coreDistSm[coreDistSm$N>minDay,]
rSmall$N<-NULL

 
mdata4 <- melt(rSmall)
if(printPlots) write.table(as.data.frame(rSmall), file = paste(rDir,"retentionSmall.csv",sep=""),sep = "\t" ,row.names = FALSE)

plot13 <- ggplot(mdata4, aes(y=value,x=variable,fill=variable)) +
 geom_boxplot(outlier.colour="red", outlier.shape=16,outlier.size=2, notch=FALSE)+
labs(title="",x="Core Distribution", y = "Perc.")+
 scale_fill_brewer(palette="Blues") +   theme_minimal() + theme(legend.position='none',text = element_text(size=myFontSize-2))
plot13


if(printPlots){
ggsave(plot=plot13,file="exp1coreboxplotAll.pdf",device="pdf",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
ggsave(plot=plot13,file="exp1coreboxplotAll.png",device="png",width=10,height=6,units=c("in"),dpi=1200,path=pDir)
}


