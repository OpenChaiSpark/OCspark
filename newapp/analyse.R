## I'll have the input image create time from /sparcle-log/simulate.
## And the output result file create time from stat -c "%y %n" output/*.jpg.result.
## So I can:
##
##   1. Calculate the lag between each successive result file create time.
##   2. Calculate the lag between the create time of a given input image and
##      its result file (via a join).
##
## Note: I could also try to capcture:
##
##      via dummytf.pl output:
##            timestamp when start processing
##            timestamp when finished processing (effectively a constant delta)
##
##      via newapp:
##            timestamp when result file read
##            timestamp when result file written (as part of concat)

## Note: cpu sees to be on a different TZ (7 hours ahead) so we subtract
## 7 hours from s and d's timestamps.

## Process the output result file timestamps.

d <- read.csv("output.stat", sep=" ", header=FALSE) %>%
    mutate(n = as.integer(str_extract(V4, "[0-9]+"))) %>%
    mutate(result.create.time =
               as.POSIXct(strptime(paste(V1, V2), "%Y-%m-%d %H:%M:%OS"))) %>%
    select(-c(V1, V2, V3, V4)) %>%
    mutate(result.create.time = result.create.time - 7 * 60 * 60)

## Lag the (time-sorted) result file timestamps) to see inter-result
## arrival times.

lagged <- d %>%
    arrange(result.create.time) %>%
    mutate(delta = as.double((result.create.time - lag(result.create.time)) * 1000)) %>%
    tail(-1)

plot(density(lagged$delta))
summary(lagged$delta)

positive.lag <- lagged %>% filter(delta > 0)

plot(density(positive.lag$delta))
summary(positive.lag$delta)

## Find any missing outputs (typically didn't get processed in time).

setdiff(1:max(d$n), d$n)

## Process the simulator log.

s <- read.csv("simulate", header = FALSE, sep = " ") %>%
    tail(-1) %>%
    mutate(t = paste(V4, V5)) %>%
    select(-c(V4, V5)) %>%
    setNames(c("n", "sim.period", "sim.delta", "sim.create.time")) %>%
    mutate(n = as.integer(str_replace(n, "^n=", ""))) %>%
    mutate(sim.period = as.integer(str_replace(sim.period, "^p=", ""))) %>%
    mutate(sim.delta = as.double(str_replace(sim.delta, "^d=", ""))) %>%
    mutate(sim.create.time = str_replace(sim.create.time, "^t=", "")) %>%
    mutate(sim.create.time =
               as.POSIXct(strptime(sim.create.time, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(sim.create.time = sim.create.time - 7 * 60 * 60)

files.tmp <- list.files("output") %>%
    lapply(function(x) paste("output", x, sep = "/")) %>%
    unlist %>%
    lapply(readLines) %>%
    lapply(t)

## Check for bad result files (eg. "(null)" instead of dummytf output).

files.bad <- files.tmp %>% keep(function(x) length(x) != 13)
files.good <- files.tmp %>% keep(function(x) length(x) == 13)

length(files.bad)

files.bad

files <- files.good %>%
    keep(function(x) length(x) == 13) %>%
    unlist %>%
    matrix(nrow = length(files.good), byrow = TRUE) %>%
    as.data.frame %>%
    mutate(V1 = str_replace(V1, ".+: ", ""),
           V2 = str_replace(V2, ".+: ", ""),
           V3 = str_replace(V3, ".+: ", ""),
           V4 = str_replace(V4, ".+: ", ""),
           V5 = str_replace(V5, ".+: ", ""),
           V6 = str_replace(V6, ".+: ", ""),
           V7 = str_replace(V7, ".+: ", ""),
           V8 = str_replace(V8, ".+: ", ""),
           V9 = str_replace(V9, ".+: ", ""),
           V10 = str_replace(V10, ".+: ", ""),
           V11 = str_replace(V11, ".+: ", ""),
           V12 = str_replace(V12, ".+: ", ""),
           V13 = str_replace(V13, ".+: ", "")) %>%
    mutate(V1 = str_replace(V1, ".+/", "")) %>%
    mutate(V1 = str_extract(V1, "[0-9]+")) %>%
    mutate(V2 = as.POSIXct(strptime(V2, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V3 = as.POSIXct(strptime(V3, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V10 = as.POSIXct(strptime(V10, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V11 = as.POSIXct(strptime(V11, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V12 = as.POSIXct(strptime(V12, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V13 = as.POSIXct(strptime(V13, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V1 = as.integer(V1),
           V5 = as.integer(V5),
           V6 = as.integer(V6),
           V7 = as.integer(V7),
           V8 = as.integer(V8),
           V9 = as.integer(V9)) %>%
    setNames(c("n",
               "tf.start",
               "tf.stop",
               "tf.duration",
               "na.total.duration",
               "na.delay.duration",
               "na.pending.results",
               "na.pending.images",
               "na.processed.results",
               "na.total.start",
               "na.total.stop",
               "na.delay.start",
               "na.delay.stop"))

## Join all three sources on image number and calculate delta-times.

data <- files %>%
    inner_join(s, by="n") %>%
    inner_join(d, by="n") %>%
    arrange(n) %>%
    mutate(delta.full = result.create.time - sim.create.time,
           delta.start.tf = tf.start - sim.create.time,
           delta.finish.tf = tf.stop - sim.create.time,
           delta.start.newapp = na.total.start - sim.create.time,
           delta.pause.newapp = na.delay.start - sim.create.time,
           delta.finish.newapp = na.total.stop - sim.create.time)


## Look at pending/processed stats.

max(data$na.pending.images)
max(data$na.pending.results)
max(data$na.processed.results)

data %>% group_by(na.pending.results) %>% summarize(n())
data %>% group_by(na.pending.images) %>% summarize(n())
data %>% group_by(na.processed.results) %>% summarize(n())

dd <- data %>% head(500)

plot(x = dd$n, y = dd$delta.full, type = 'l')
lines(x = dd$n, y = dd$delta.start.tf, col = 'red')
lines(x = dd$n, y = dd$delta.finish.tf, col = 'orange')
lines(x = dd$n, y = dd$delta.start.newapp,col = 'green')
