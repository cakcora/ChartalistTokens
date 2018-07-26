library(ethr)

library(data.table)

rm(list = ls())


remove_na <- function(x){
  x$to[sapply(x$to, is.null)] <- "addressUnKnown"
  return(x)
  
}



process <- function(j){

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
transaction_data <- as.data.frame(do.call("rbind", resultlis)) 
rm(resultlis,i)

header_data <- as.data.frame(header_data)
transaction_data <- as.data.frame(transaction_data)


#header_data<-remove_na(header_data)

transaction_data<-remove_na(transaction_data)

#header_data[is.na(header_data)] <- 0
transaction_data$to[is.na(transaction_data$to)]<-"0"
dir<-"D:/Ethereum/fromR"
fwrite(header_data, file = paste(dir,"/header_", j,".csv" ,sep = ""),na="",row.names=FALSE)
trFile<-paste(dir,"/transaction_", j,".csv" ,sep = "")
fwrite(transaction_data, file =trFile ,na="",row.names=FALSE)
}


j = 4900000
process(j)
process(j+1)
