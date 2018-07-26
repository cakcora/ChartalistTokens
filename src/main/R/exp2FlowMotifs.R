require(plyr)
require(ggplot2)
 
 

rm(list=ls(all=TRUE))

wDir<-"D:/Dropbox/Publications/PostDoc work/3 - Chartalist/"
wDir<-"C:/Users/cakcora/Dropbox/Publications/PostDoc work/3 - Chartalist/"
pDir<-paste(wDir,"/figures/",sep="")

file<-"results/exp2Flow/FlowMotifs.txt"
 
 #http://vlado.fmf.uni-lj.si/pub/networks/doc/triads/triads.pdf
  data <- read.csv(file=paste(wDir,file,sep=""),sep="\t",header=F)
   colnames(data)<-c("token","year","period","vertex","edge",
                       "m1","m2","m3","m4","m5","m6","m7","m8","m9","m10","m11","m12","m13","m14","m15","m16")
 data$m1<-NULL
  data$ssum<-data$m4+data$m5+data$m6+data$m7+data$m8+data$m9+data$m10+data$m11+data$m12+
    data$m13+data$m14+data$m15+data$m16
 
motifs<-   ddply(data, .(token), summarize,
         med10 = median(m10),
         med4 = median(m4),
         med5 = median(m5),
         med6 = median(m6),
         med7 = median(m7),
         med9= median(m9),
         med=median(ssum)
)  
motifs$med10<-motifs$med10/motifs$med
motifs$med4<-motifs$med4/motifs$med
motifs$med5<-motifs$med5/motifs$med
motifs$med6<-motifs$med6/motifs$med
motifs$med7<-motifs$med7/motifs$med
motifs$med9<-motifs$med9/motifs$med
motifs

ggplot(motifs, aes(x=med10)) + 
  geom_histogram()+theme_classic()
hist(motifs$med10)

x<-motifs$token
x<-cbind.data.frame(x,motifs$med9)
x$type<-"p9"
colnames(x)<-c("token","med","motif")
x


y<-motifs$token
y<-cbind.data.frame(y,motifs$med10)
y$type<-"p10"
colnames(y)<-c("token","med","motif")
bData<-rbind.data.frame(x,y)
bData

bp <- ggplot(bData, aes(y=med,x=motif,fill=motif)) + geom_boxplot(outlier.colour="red", outlier.shape=16,
                                                                      outlier.size=2, notch=FALSE)+  labs(title="",x="Motif", y = "Perc.")
bp<-bp + scale_fill_brewer(palette="Blues") +   theme_minimal()+ theme(legend.position='none')
bp


z<-motifs$token
z<-cbind.data.frame(z,motifs$med6)
z$type<-"p6"
colnames(z)<-c("token","med","motif")
z
z2<-motifs$token
z2<-cbind.data.frame(z2,motifs$med4)
z2$type<-"p4"
colnames(z2)<-c("token","med","motif")
z2
t<-motifs$token
t<-cbind.data.frame(t,motifs$med5)
t$type<-"p5"
colnames(t)<-c("token","med","motif")
bData2<-rbind.data.frame(z,z2,t)
bData2

bp2 <- ggplot(bData2, aes(y=med,x=motif,fill=motif)) + 
  geom_boxplot(outlier.colour="red", outlier.shape=16,outlier.size=2, notch=FALSE)+
  labs(title="",x="Motif", y = "Perc.")+
  scale_fill_brewer(palette="Blues") +   
  theme_minimal()+ theme(legend.position='none')
bp2

#ggsave("motif4_5_6.pdf", plot = bp2, device = "pdf", path = "D:/Dropbox/Publications/PostDoc work/Chartalist/figures/",
#       scale = 1, width = 5, height = 3, units = c("in"),
#       dpi = 1500, limitsize = TRUE)
#ggsave("motif9_10.pdf", plot = bp, device = "pdf", path = "D:/Dropbox/Publications/PostDoc work/Chartalist/figures/",
#       scale = 1, width = 5, height = 3, units = c("in"),
#       dpi = 1500, limitsize = TRUE)
bData2

