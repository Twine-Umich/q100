default: compile

base_dir   = $(abspath .)
src_dir    = $(base_dir)/src/main
gen_dir    = $(base_dir)/generated-src
out_dir    = $(base_dir)/output_files
in_dir		= $(base_dir)/input_files
verilator_dir = $(base_dir)/obj_dir
SBT       = sbt
SBT_FLAGS = -ivy $(HOME)/.ivy2
test_dir = $(base_dir)/tests
test = $(test_dir)/test.cpp

sbt:
	$(SBT) $(SBT_FLAGS)

compile: $(gen_dir)/Tile.v

$(gen_dir)/Tile.v: $(wildcard $(src_dir)/scala/*.scala)
	$(SBT) $(SBT_FLAGS) "run $(gen_dir)"

CXXFLAGS += -std=c++11 -Wall -Wno-unused-variable

Verilator-Tile: $(gen_dir)/Tile.v $(test)
	verilator -cc $<
	g++ -I $(verilator_dir) -I/usr/share/verilator/include $(verilator_dir)/*.cpp $(test) /usr/share/verilator/include/verilated.cpp -o $@

$(test): $(test_dir)/config.json $(test_dir)/test_template.cpp
	cd tests; cog -o test.cpp test_template.cpp ; cd ..

run-verilator: Verilator-Tile
	./$< 2>/dev/null

clean:
	rm -rf $(gen_dir) $(out_dir)/* Verilator-Tile obj_dir $(test_dir)/test.cpp 


.PHONY: sbt compile verilator clean 
