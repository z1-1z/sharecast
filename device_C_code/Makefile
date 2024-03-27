
CROSS_COMPILE=

BIN=test

app_dir=./app

include ./app_src.mk

include rules.mak

CFLAGS += -pipe -O2 -g -ffunction-sections -fdata-sections -Wundef -Wall
CFLAGS += -fsigned-char

CFLAGS+=-m32
LDFLAGS+=-m32

LDFLAGS += -Wl,--gc-sections 
LDFLAGS	+= -Wl,--start-group

LDFLAGS += -Wl,-Bstatic #force to use static lib

LDFLAGS += -lz

LDFLAGS += -Wl,-Bdynamic #change to use shared lib first
LDFLAGS += -lm -ldl -lpthread -lrt

LDFLAGS	+= -Wl,--end-group
