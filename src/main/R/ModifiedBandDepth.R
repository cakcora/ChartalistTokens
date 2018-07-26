

#IGNORE THIS PART UNTIl BELOW
rm(list = ls())

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
a<-read.csv(file="D:/Dropbox/Publications/PostDoc work/3 - Chartalist/results/exp2flow/coral.txt",sep="\t",header=T)
hist(a$coreVal)
summary(a)
ggplot(data=a,aes(x=edgefactor,weightfactor))+geom_point()+scale_x_log10()+scale_y_log10()
