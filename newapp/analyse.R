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

library(dplyr)
library(ggvis)
library(ggplot2)
library(stringr)
library(purrr)
library(tidyr)

## Process the output result file timestamps.

d <- read.csv("output.stat", sep=" ", header=FALSE) %>%
    mutate(n = as.integer(str_extract(V4, "[0-9]+"))) %>%
    mutate(result.create.time =
               as.POSIXct(strptime(paste(V1, V2), "%Y-%m-%d %H:%M:%OS"))) %>%
    select(-c(V1, V2, V3, V4)) %>%
    mutate(result.create.time = result.create.time - 7 * 60 * 60) %>%
    arrange(n)

## Lag the (time-sorted) result file timestamps) to see inter-result
## arrival times.

lagged <- d %>%
    arrange(result.create.time) %>%
    mutate(delta = as.double((result.create.time - lag(result.create.time)) * 1000)) %>%
    tail(-1)

plot(density(lagged$delta))
summary(lagged$delta)

print("Interarrival times:")
print(summary(lagged$delta))

ll <- lagged %>% mutate(i = 1:nrow(lagged)) %>% head(500)

ll %>% ggplot(aes(x = i, y = delta)) + geom_line(color='darkgreen')

ggsave("interarrival.pdf", width = 32, height = 18, units = "cm")


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

files.bad <- files.tmp %>% keep(function(x) length(x) != 17)
files.good <- files.tmp %>% keep(function(x) length(x) == 17)

length(files.bad)

##files.bad

files <- files.good %>%
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
           V13 = str_replace(V13, ".+: ", ""),
           V14 = str_replace(V14, ".+: ", ""),
           V15 = str_replace(V15, ".+: ", ""),
           V16 = str_replace(V16, ".+: ", ""),
           V17 = str_replace(V17, ".+: ", "")) %>%
    mutate(V2 = str_replace(V2, ".+/", "")) %>%
    mutate(V3 = str_replace(V3, ".+/", "")) %>%
    mutate(V2 = str_extract(V2, "[0-9]+")) %>%
    mutate(V3 = str_extract(V3, "[0-9]+")) %>%
    mutate(V4 = as.POSIXct(strptime(V4, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V5 = as.POSIXct(strptime(V5, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V6 = as.POSIXct(strptime(V6, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V14 = as.POSIXct(strptime(V14, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V15 = as.POSIXct(strptime(V15, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V16 = as.POSIXct(strptime(V16, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V17 = as.POSIXct(strptime(V17, "%Y-%m-%d %H:%M:%OS"))) %>%
    mutate(V2 = as.integer(V2),
           V3 = as.integer(V3),
           V9 = as.integer(V9),
           V10 = as.integer(V10),
           V11 = as.integer(V11),
           V12 = as.integer(V12),
           V13 = as.integer(V13)) %>%
    setNames(c("host",
               "n",
               "n.copy",
               "tf.input.create.time",
               "tf.start",
               "tf.stop",
               "tf.task.duration",
               "tf.total.duration",
               "na.total.duration",
               "na.delay.duration",
               "na.pending.results",
               "na.pending.images",
               "na.processed.results",
               "na.total.start",
               "na.total.stop",
               "na.delay.start",
               "na.delay.stop")) %>%
    select(-n.copy)

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
           delta.finish.newapp = na.total.stop - sim.create.time,
           tf.start.delay=tf.start - tf.input.create.time,
           image.copy.delay=tf.input.create.time - sim.create.time)

## Look at pending/processed stats.

max(data$na.pending.images)
max(data$na.pending.results)
max(data$na.processed.results)

data %>% group_by(na.pending.results) %>% summarize(n())
data %>% group_by(na.pending.images) %>% summarize(n())
data %>% group_by(na.processed.results) %>% summarize(n())

dd <- data %>% head(500)

#plot(x = dd$n, y = dd$delta.full, type = 'l')
#lines(x = dd$n, y = dd$delta.start.tf, col = 'red')
#lines(x = dd$n, y = dd$delta.finish.tf, col = 'orange')
#lines(x = dd$n, y = dd$delta.start.newapp,col = 'green')

dd.by.metric <- dd %>%
    select(n, delta.full, delta.start.tf, delta.finish.tf,
           delta.start.newapp, delta.finish.newapp) %>%
    gather("metric", "seconds", -n) %>%
    group_by(metric)

dd.by.metric %>%
    ggvis(~n, ~seconds) %>%
    layer_paths(stroke = ~metric) %>%
    add_legend('stroke')

dd.by.metric %>% ggplot(aes(x=n, y=seconds, group=metric, color=metric)) + geom_line()

ggsave("bymetric.pdf", width = 32, height = 18, units = "cm")

## Group by host.

data %>%
    group_by(host) %>%
    summarize(n(),
              mean(delta.full),
              mean(delta.pause.newapp),
              mean(delta.start.tf),
              mean(delta.finish.tf),
              mean(delta.start.newapp),
              mean(delta.pause.newapp),
              mean(delta.finish.newapp)) %>%
    as.data.frame

data %>%
    group_by(host) %>%
    summarize(n(), mean(delta.full), median(delta.full), max(delta.full))

cc <- dd %>% head(200)

cc %>%
    select(host, n, delta.full, delta.start.tf, delta.finish.tf,
           delta.start.newapp, delta.finish.newapp) %>%
    gather("metric", "seconds", -n, -host) %>%
    ggvis(~n, ~seconds) %>%
    group_by(host, metric) %>%
    layer_points(stroke = ~metric, shape = ~host) %>%
    add_legend('stroke', 'shape', properties = legend_props(legend = list(y = 120)))

## Plot delta.full vs n, grouped by host.

dd.by.host <- dd %>%
    select(host, n, delta.full) %>%
    group_by(host)

dd.by.host %>%
    ggvis(~n, ~delta.full) %>%
    layer_paths(stroke = ~host) %>%
    add_legend('stroke')

dd.by.host %>% ggplot(aes(x=n, y=delta.full, group=host, color=host)) + geom_line()

ggsave("byhost.pdf", width = 32, height = 18, units = "cm")

## Look at image copy delay.

dd %>% ggplot(aes(x=n, y=slave.copy.delay)) + geom_line()

dd.by.metric.2 <- dd %>%
    select(n, delta.full, image.copy.delay) %>%
    gather("metric", "seconds", -n) %>%
    group_by(metric)

dd.by.metric.2 %>%
    ggvis(~n, ~seconds) %>%
    layer_paths(stroke = ~metric) %>%
    add_legend('stroke')

dd.by.metric.2 %>% ggplot(aes(x=n, y=seconds, group=metric, color=metric)) + geom_line()

ggsave("bymetric2.pdf", width = 32, height = 18, units = "cm")
