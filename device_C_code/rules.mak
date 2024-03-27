
CC=$(CROSS_COMPILE)gcc
LD=$(CROSS_COMPILE)gcc
CPP=$(CROSS_COMPILE)g++
STRIP=$(CROSS_COMPILE)strip

BINARY_PATH += ./out/bin
OBJECT_PATH += ./out/obj

ifneq ($(BIN),)
BIN_TARGET+=$(BINARY_PATH)/$(BIN)
endif
ifneq ($(STATIC_LIB),)
STATIC_TARGET+=$(BINARY_PATH)/$(STATIC_LIB)
endif
ifneq ($(SHARE_LIB),)
+=$(BINARY_PATH)/$(SHARE_LIB)
endif

TARGET+=$(BIN_TARGET) $(STATIC_TARGET) $(SHARE_TARGET)

SOURCES+=$(foreach DIR,$(src_dir),$(wildcard $(DIR)/*.c))
SOURCES_CPP+=$(foreach DIR,$(src_dir),$(wildcard $(DIR)/*.cpp))

OBJECTS+=$(addsuffix .o, $(addprefix $(OBJECT_PATH)/, $(basename $(SOURCES))))
OBJECTS+=$(addsuffix .o, $(addprefix $(OBJECT_PATH)/, $(basename $(SOURCES_CPP))))

DEPENDS+=$(addsuffix .d, $(OBJECTS))

CFLAGS += $(foreach dir, $(inc_dir),  -I$(dir))
LDFLAGS += $(foreach dir, $(lib_dir),  -L$(dir))

AT=@
.PHONY : all clean prepare build

all : build

clean :
	$(AT)rm -rf $(BINARY_PATH)
	$(AT)rm -rf $(OBJECT_PATH)

prepare : $(BINARY_PATH) $(OBJECT_PATH)

build : prepare
	$(AT)$(MAKE) $(TARGET)

$(BINARY_PATH):
	$(AT)test -d $@ || mkdir -p $@

$(OBJECT_PATH):
	$(AT)test -d $@ || mkdir -p $@

$(BIN_TARGET) : $(OBJECTS)
	$(AT)echo -e "generating bin \033[032m[$(CC)]\033[0m": $@
	$(AT)$(LD) -o $@ $(OBJECTS) $(LDFLAGS)
	$(AT)ls -hls $@

$(STATIC_TARGET) : $(OBJECTS)
	$(AT)echo -e "generating static \033[032m[$(CC)]\033[0m": $@
	$(AT)$(AR) r $@ $(OBJECTS)
	$(AT)ls -hls $@

$(SHARE_TARGET) : $(OBJECTS)
	$(AT)echo -e "generating share \033[032m[$(CC)]\033[0m": $@
	$(AT)$(LD) -shared -o $@ $(OBJECTS)
	$(AT)ls -hls $@

define make-cmd-cc
$2 : $1
	$(AT)test -d $(dir $2) || mkdir -p $(dir $2)
	$(AT)echo -e "compiling  \033[032m[$(CC)]\033[0m": $$<
	$(AT)$$(CC) $$(CFLAGS) -MMD -MT $$@ -MF $$@.d -c -o $$@ $$<
endef

define make-cmd-cpp
$2 : $1
	$(AT)test -d $(dir $2) || mkdir -p $(dir $2)
	$(AT)echo -e "compiling  \033[032m[$(CPP)]\033[0m": $$<
	$(AT)$$(CPP) $$(CFLAGS) $$(CPPFLAGS) -MMD -MT $$@ -MF $$@.d -c -o $$@ $$<
endef

$(foreach afile, $(SOURCES), $(eval $(call make-cmd-cc, $(afile), $(addsuffix .o, $(addprefix $(OBJECT_PATH)/, $(basename $(afile)))))))
$(foreach afile, $(SOURCES_CPP), $(eval $(call make-cmd-cpp, $(afile), $(addsuffix .o, $(addprefix $(OBJECT_PATH)/, $(basename $(afile)))))))

-include $(DEPENDS)
