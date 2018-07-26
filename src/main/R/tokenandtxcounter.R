require(plyr)
require(ggplot2)


rm(list=ls(all=TRUE))

folder <- "D:/Dropbox/Publications/PostDoc work/3 - Chartalist/"
 
data <-(read.csv(paste(folder,"results/ERC20TxInTime.txt",sep=""),sep="\t",header=T))
 
data$myTime<-as.Date(strptime(paste(data$year, data$day,sep=" "), format="%Y %j"))
data<-data[data$myTime!=max(data$myTime),]


 

m12 <- ggplot(data[data$transaction>0,],aes(x=data$myTime,y=data$transaction/1000))+geom_point(size=1,color="red")
m12<- m12+geom_smooth(span = 0.2,method = 'loess')
m12<- m12+scale_x_date(name="time")+ theme_minimal()
m12<- m12+scale_y_log10(name="Tx count (K)")+ theme(text = element_text(size=16)) 
m12<-m12+theme(text = element_text(size=20),legend.text = element_text(size=20))
m12
 
 


ggsave(filename=paste(folder,"figures/tokenTx.pdf",sep=""),plot=m12,width=10,height=6,unit="in")
ggsave(filename=paste(folder,"figures/tokenTx.png",sep=""),plot=m12,width=10,height=6,unit="in")

 


 
data <-(read.csv(paste(folder,"results/Erc20ContractInTime.txt",sep=""),sep="\t",header=T))
data$myTime<-as.Date(strptime(paste(data$year, data$day,sep=" "), format="%Y %j"))
data<-data[data$myTime!=max(data$myTime),]




m12 <- ggplot(data,aes(x=data$myTime,y=data$contract))+geom_point(size=1,color="red")  
m12<- m12+scale_x_date(name="time")+ theme_minimal()

m12<- m12+geom_smooth(span = 0.2,method = 'loess')
m12<- m12+scale_y_log10(name="Contract count")+theme(text = element_text(size=20),legend.text = element_text(size=20))
m12


ggsave(filename=paste(folder,"figures/tokenContract.pdf",sep=""),plot=m12,width=10,height=6,unit="in")
ggsave(filename=paste(folder,"figures/tokenContract.png",sep=""),plot=m12,width=10,height=6,unit="in")


