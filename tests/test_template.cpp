#include <stdlib.h>
#include <cstdint>
#include <iostream>
#include <fstream>
#include "VTile.h"
#include "verilated.h"
#include <sstream>
using namespace std;

unsigned long long num_of_cycles = 0;

int reset(VTile *tb){ // set the reset to hi
	tb->reset = 1;
	tb->eval();
	tb->clock = 1;
	tb->eval();
	tb->clock = 0;
	tb->eval();
	return 0;
}

int tiktok(VTile *tb){ // tik 1 cycle
	tb->clock = 1;
	tb->eval();
	tb->clock = 0;
	tb->eval();
	num_of_cycles ++;
	return 0;
}

int main(int argc, char **argv) {
	// Initialize Verilators variables
	Verilated::commandArgs(argc, argv);

	// Create an instance of our module under test
	VTile *tb = new VTile;
	ifstream in_dat_file;
	ofstream out_dat_file;
	ofstream stats_file;
	/* [[[cog
import cog
import json
f = open('config.json')
data = json.load(f)
cog.outl("	in_dat_file.open(\"%s\",ifstream::in);" % data["raw_dat_file_name"])
cog.outl("	out_dat_file.open(\"%s\",ofstream::out);" % data["raw_output_file_name"])
cog.outl("	stats_file.open(\"%s\",ofstream::out);" % data["stats_file_name"])
]]] */
// [[[end]]]

	tb->ctrl_in_valid = 0;
	int hit_end_of_line = 0;
	int eof = 0;
	reset(tb);
	tb->reset = 0;
	tb->ctrl_out_ready = 1;
	tb->eval();
	while(!Verilated::gotFinish()) {
		tiktok(tb);
		if(tb->ctrl_in_ready && !hit_end_of_line){
			string str;
			if(getline(in_dat_file, str)){
				istringstream is(str);
				tb->ctrl_in_valid = 1;
				tb->in_EOF = 0;
/* [[[cog
import cog
import json
f = open('config.json')
data = json.load(f)
cog.outl("				uint%s_t d = 0;"%data['XLEN'])
for i in range(0,data["num_of_col"]):
	cog.outl("				is >> d;")
	cog.outl("				tb->in_dat_%s = d;"% i)
]]] */
// [[[end]]]
			}else{
				tb->in_EOF = 1;
				tb->ctrl_in_valid = 1;
				hit_end_of_line = 1;
				in_dat_file.close();
			}
		}else{
			tb->in_EOF = 0;
			tb->ctrl_in_valid = 0;
		}
		if(tb->ctrl_out_valid){
			if(!tb->out_EOF){
/* [[[cog
import cog
import json
f = open('config.json')
data = json.load(f)
for i in range(0,data["num_of_col_expected"]):
	cog.outl("				out_dat_file << tb->out_dat_%s<<\" \";" %i)
]]] */
// [[[end]]]
				out_dat_file<<endl;
			}else{
				out_dat_file.close();
				stats_file << "Total Cycle: "<< num_of_cycles << endl;
				stats_file.close();
				exit(EXIT_SUCCESS);
			}
		}
		tb->eval();
	} exit(EXIT_SUCCESS);
}