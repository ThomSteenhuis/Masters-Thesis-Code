
arma <- function(cat,p,q,noPers)
{
  data <- read.table("C:/Users/emp5220514/Desktop/git/MasterThesis/src/data/prepared_data.txt",header = T,sep="\t")
  ts <- getTimeseries(data,cat)
  train <- getTrainingSet(ts)
  val <- getValidationSet(ts)
  test <- getTestingSet(ts)
  noDifs <- determineNoDifs(train)
  tsDifs <- convertTimeseries(ts,noDifs)
  trainDifs <- getTrainingSet(tsDifs)
  
  model <- arima(trainDifs,order = c(p,0,q))
  coefs <- model$coef[p+q+1]
  
  for(idx in 2:length(model$coef))
    coefs[idx] <- model$coef[idx-1]
  
  coefs[p+q+2] <- model$sigma2
  coefs[p+q+3] <- model$loglik
  coefs[p+q+4] <- model$aic
  
  return(coefs)
}

deriveTimeseries <- function(ts,tsDifs,noDifs)
{
  if(noDifs == 0)
  {
    return (tsDifs)
  }else
  {
    output <- c()
    
    for(idx in 1:noDifs)
      output[idx] <- ts[idx]
    
    firstVals <- output[noDifs]
    
    if(noDifs > 1)
    {
      tmp1 <- c()
      for(idx in 1:length(output))
        tmp1[idx] <- output[idx]
      
      for(idx1 in 2:noDifs)
      {
        tmp2 <- c()
        
        for(idx2 in 1:(noDifs-idx1+1))
          tmp2[idx2] <- tmp1[idx2+1] - tmp1[idx2]
        
        firstVals[idx1] <- tmp2[noDifs-idx1+1]
        tmp1 <- c()
        
        for(idx2 in 1:length(tmp2))
          tmp1[idx2] <- tmp2[idx2]
      }
    }
    
    tmp1 <- c()
    for(idx in 1:length(tsDifs))
      tmp1[idx] <- tsDifs[idx]
    
    for(idx1 in noDifs:1)
    {
      tmp2 <- firstVals[idx1]
      
      for(idx2 in 1:length(tmp1))
        tmp2[idx2+1] <- tmp2[idx2] + tmp1[idx2]
      
      tmp1 <- c()
      for(idx2 in 2:length(tmp2))
        tmp1[idx2-1] <- tmp2[idx2]
    }
    
    for(idx in 1:length(tmp1))
      output[idx+noDifs] <- tmp1[idx]
    
    return (output);
  }
}

forecast <- function(ts,p,q,coefs,s2,error,noPers)
{
  f <- c()
  for(idx in 1:p)
    f[idx] <- ts[idx]
  
  for(idx1 in (p+1):length(ts))
  {
    f[idx1] = coefs[p+q+1];
    
    for(idx2 in (idx1-p):(idx1-1))
      f[idx1] = f[idx1] + coefs[idx1-idx2]*(ts[idx2]-coefs[p+q+1]);
      
    for(idx2 in max(0,idx1-q):(idx1-1) )
      f[idx1] = f[idx1] + coefs[idx1-idx2+p]*error[idx2];
  }
  
  if(noPers > 1)
  {
    forecast(f,p,q,coefs,s2,noPers-1)
  }else if(noPers == 1)
  {
    return (f)
  }
}

convertTimeseries <- function(ts,noDifs)
{
  temp1 <- c()
  
  for(idx in 1:length(ts) )
    temp1[idx] <- ts[idx]
  
  if(noDifs > 0)
  {
    for(idx1 in 1:noDifs)
    {
      temp2 <- c()
      for(idx2 in 1:(length(temp1)-1))
        temp2[idx2] <- temp1[idx2+1] - temp1[idx2]
      
      temp1 <- c()
      for(idx2 in 1:length(temp2))
        temp1[idx2] <- temp2[idx2]
    }
  }
  
  return (temp1)
}

getTrainingSet <- function(ts)
{
  firstIndex <- 1
  
  while(ts[firstIndex] == 0)
    firstIndex = firstIndex + 1
  
  lastIndex = floor(0.6*(length(ts)+1-firstIndex)) + firstIndex
  
  output <- c()
  for(idx in firstIndex:(lastIndex-1))
    output[idx-firstIndex+1] <- ts[idx]
  
  return (output)
}

getValidationSet <- function(ts)
{
  firstIndex <- 1
  
  while(ts[firstIndex] == 0)
    firstIndex = firstIndex + 1
  
  firstIndex2 = floor(0.6*(length(ts)+1-firstIndex)) + firstIndex
  lastIndex = floor(0.8*(length(ts)+1-firstIndex)) + firstIndex
  
  output <- c()
  for(idx in firstIndex2:(lastIndex-1))
    output[idx-firstIndex2+1] <- ts[idx]
  
  return (output)
}

getTestingSet <- function(ts)
{
  firstIndex <- 1
  
  while(ts[firstIndex] == 0)
    firstIndex = firstIndex + 1
  
  firstIndex2 = floor(0.8*(length(ts)+1-firstIndex)) + firstIndex
  lastIndex = length(ts)
  
  output <- c()
  for(idx in firstIndex2:(lastIndex-1))
    output[idx-firstIndex2+1] <- ts[idx]
  
  return (output)
}

determineNoDifs <- function(ts)
{
  temp1 <- c()
  temp2 <- c()
  
  for(idx in 1:length(ts))
  {
    temp1[idx] = ts[idx];
    temp2[idx] = ts[idx];
  }
  
  noDifs = 0;
  
  while(DickyFuller(temp1))
  {
    noDifs = noDifs + 1;
    temp1 = c();
    
    for(idx in 1:(length(temp2)-1))
      temp1[idx] <- temp2[idx+1]-temp2[idx];
    
    temp2 <- c();
    
    for(idx in 1:length(temp1))
      temp2[idx] = temp1[idx];
  }
  
  return (noDifs)
}

DickyFuller <- function(ts)
{
  Y1 = c()
  deltaY = c()
  errors = c()
  
  for(idx in 1:(length(ts)-1) )
  {
    Y1[idx] = ts[idx]
    deltaY[idx] = ts[idx+1] - ts[idx]
  }
  
  delta =  (Y1 %*% deltaY) / (Y1 %*% Y1)
  
  for(idx in 1:length(Y1) )
    errors[idx] = (deltaY[idx] - delta*Y1[idx])
  
  s2 = (errors %*% errors) / (length(Y1)-1)
  se = sqrt(s2 / (Y1 %*% Y1) )
  tstat = abs(delta/se)
  
  if(tstat < 1.95)
    return (T)
  
  return (F)
}

getTimeseries <- function(data,cat)
{
  if(cat == "2200EVO")
    x <- as.numeric(data$X2200EVO)
  else if(cat == "8800FCQ, RFID")
    x <- (as.numeric(data$X8800FCQ..RFID))
  else if(cat == "TCB & Chameo")
    x <- (as.numeric(data$TCB...Chameo))
  else if(cat == "DB2100")
    x <- (as.numeric(data$DB2100))
  else if(cat == "DB2009")
    x <- (as.numeric(data$DB2009))
  else if(cat == "Molding")
    x <- (as.numeric(data$Molding))
  else if(cat == "FCL")
    x <- (as.numeric(data$FCL))
  else if(cat == "FSL")
    x <- (as.numeric(data$FSL))
  else if(cat == "Plating")
    x <- (as.numeric(data$Plating))
  
  firstIndex <- 1
  
  while(x[firstIndex] == 0)
    firstIndex = firstIndex + 1
  
  output <- c()
  for(idx in firstIndex:length(x))
    output[idx-firstIndex+1] <- x[idx]
  
  return (output)
}