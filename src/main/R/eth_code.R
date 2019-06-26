install.packages("devtools")
devtools::install_github("BSDStudios/ethr")
require(ethr)
library(data.table)

#start geth from cmd with geth -rpc

rm(list = ls())
MDIR<-"D:/Ethereum/fromR/"

crawl<-function(j){
# paste is used to concatenate two strings in R 
# we need to concatenate strings here because geth takes in 0x+hex_val_of_blocknumber 
# sprintf converts the numeric value to hex 
block_num = paste("0x", sprintf("%x",j), sep="")


# example of how the call is made to 
# eth_getBlockByNumber returns the block header information and transactions list for a block 
block_data=eth_getBlockByNumber(block_num,full_list = TRUE)

rm(block_num)
# now , we need to get the values into a dataframe 
# splitting the data we need into two parts - block header info and transactions info 
# leaving the hex values as is , can be converted later on.
# The idea is that they will consume less space when being parsed. 

# header information  
header_list = list(difficulty = block_data$difficulty
,extra_data = block_data$extraData
,gas_limit = block_data$gasLimit
,gas_used = block_data$gasUsed
,hash= block_data$hash
,logs_bloom = block_data$logsBloom
,miner  = block_data$miner
,mix_hash = block_data$mixHash
,nonce  = block_data$nonce
,number = block_data$number
,parent_hash = block_data$parentHash
,receipts_root = block_data$receiptsRoot
,sha3_uncles  = block_data$sha3Uncles
,size = block_data$size
,state_root = block_data$stateRoot
,timestamp  = block_data$timestamp
,total_difficulty  = block_data$totalDifficulty
,transactions_root = block_data$transactionsRoot)

header_data = data.frame(header_list)

rm(header_list)

# now get the transaction information into a dataframe 
transactions_list = block_data$transactions


resultlis <- list() 
for (i in 1:length(transactions_list)){ 
  
   
  transaction = list(block_hash = transactions_list[[i]]$blockHash
                     ,block_number = transactions_list[[i]]$blockNumber
                     ,from  = transactions_list[[i]]$from
                     ,gas = transactions_list[[i]]$gas
                     ,gasprice= transactions_list[[i]]$gasPrice
                     ,hash = transactions_list[[i]]$hash
                     ,input= transactions_list[[i]]$input
                     ,nonce = transactions_list[[i]]$nonce
                     ,to = transactions_list[[i]]$to
                     ,transaction_index = transactions_list[[i]]$transactionIndex
                     ,value = transactions_list[[i]]$value
                     ,v= transactions_list[[i]]$v
                     ,r= transactions_list[[i]]$r
                     ,s= transactions_list[[i]]$s)
  
  
  resultlis[[i]] <- transaction 
  
} 


rm(block_data,transaction,transactions_list)
#transaction_data <- as.data.frame(do.call("rbind", resultlis)) 
rm(resultlis,i)


#transaction_data =  lapply(transaction_data, function(x) lapply(x, function(x) ifelse(is.null(x), NA, x))) 
 
#Transaction_data format: "",block_hash,block_number,from,gas,gasprice,hash,input,nonce,to,transaction_index,value,v,r,s
#header_data format difficulty,extra_data,gas_limit,gas_used,hash,logs_bloom,miner,mix_hash,nonce,number,parent_hash,receipts_root,sha3_uncles,size,state_root,timestamp,total_difficulty,transactions_root

 
#fwrite(transaction_data, file = paste(MDIR,"block_", j,".csv" ,sep = ""),sep=",",na="",row.names=FALSE)
return (header_data)
# remove this fwrite(header_data, file = paste(MDIR,"headers/header_", j,".csv" ,sep = ""),sep=",",na="",row.names=FALSE)
}

myFile <- paste(MDIR,"headers/header.txt" ,sep = "")
write("",file=myFile,append=FALSE)#reset
for(j in 5000001:5565630){
a<-  tryCatch({
    header<- crawl(j)
    hData<-cbind(blockNumber=j,header)
     
    fwrite(hData,file=myFile,append=TRUE)
  if(j%%100==0){
     message("processed ",j)
    
  }   
  }, warning = function(w) {
    
  }, error = function(e) {
     message("block has error: ",j)
  }, finally = {
     
  })
  
}