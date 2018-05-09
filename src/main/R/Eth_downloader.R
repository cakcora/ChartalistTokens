require(igraph)

token<-"networkeosTX"
wDir<-"D:/Ethereum/graphs/"
hDir<-"C:/Users/cakcora/Desktop/graphs/"
cDir<-wDir
edgeListFile<-paste(wDir,token,".txt",sep="")
data <- read.table(edgeListFile,header=FALSE)
colnames(data)<- c("from","to","time","weight")
data$time<-as.integer(data$time/(60*60*24))
dir<-paste(cDir,"figures/",sep="")

#do.call(file.remove, list(list.files(dir, full.names = TRUE)))
out <- split( data , f = data$time )
sapply( out , function(x){ 
  
  tryCatch(
    {
      #x<-out[[5]]
      gr<-graph.data.frame(x, directed=TRUE)
      dg <- decompose.graph(gr) # returns a list of three graphs
      max=0
      bgr =""
      for(i in dg){
        if(vcount(i)>max){
          bgr=i
          max=vcount(i)
        }
      }
      message("biggest com has",vcount(bgr)," vertices")
      gr=bgr
      
      if(vcount(gr)>300){ 
      clp <- cluster_label_prop(gr)
      class(clp)
      
      lo <- layout_with_fr(gr,niter=1000)
      
      pdf(paste(dir,token,x$time[1],".pdf",sep=""),width=20,height=12,paper='special')
      plot(clp,layout=lo,gr,vertex.label=NA, edge.arrow.size=0.5,vertex.size=5)
      dev.off()
      
      png(paste(dir,token,x$time[1],".png",sep=""), 1500, 900)
      plot(clp,layout=lo,gr,vertex.label=NA, edge.arrow.size=1.5,vertex.size=5)
      dev.off()
      
       
      }
    },
    error=function(cond) {
      message(x$time[1],cond)
       
    },
    warning=function(cond) {
      message(x$time[1],cond)
    },
    finally={
     
    }
  )   
    
})

 


