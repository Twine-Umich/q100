package q100

import chisel3._
import chisel3.util._
import chisel3.twine._
import chisel3.twine.util._

class Tilepaper_Input extends Bundle with Q100Params{
    val dat = Vec(3, UInt(XLEN.W))
    val EOF = Bool() 
}
class Tilepaper_Output extends Bundle with Q100Params{
    val reference = UInt(XLEN.W)
    val dat = UInt(XLEN.W)
    val EOF = Bool() 
}

class Tile extends TwineModule with Q100Params{

    // Declare the top layer inputs. Don't Change
    val in = IO(Input(new Tilepaper_Input))
    var out = IO(Output(new Tilepaper_Output))
    val ctrl = IO(new DecoupledIOCtrl(2,2))

    val agg1 = Module(new Aggregator)
    val agg2 = Module(new Aggregator)
		val alu = Module(new MULTISTAGE_ALU(1))
		val colSelect1= Module(new ColSelect(3,0))
		val colSelect2 = Module(new ColSelect(3,1))
		val colSelect3 = Module(new ColSelect(3,2))

		in >>> colSelect1
		in >>> colSelect2
		in >>> colSelect3

		val colFilter1 = Module(new ColFilter)
		val colFilter2 = Module(new ColFilter)
		val colFilter3 = Module(new ColFilter)
		val boolGen = Module(new BoolGen(BoolGenOP.NEQ, 0.U))

		TwineBundle(colSelect1.out.dat, colSelect1.out.EOF) >>> boolGen

		boolGen >>> colFilter1

		TwineBundle(colSelect2.out.dat,boolGen.out.cmp, colSelect2.out.EOF) >>> colFilter2
		TwineBundle(colSelect3.out.dat,boolGen.out.cmp, colSelect3.out.EOF) >>> colFilter3
    TwineBundle(colFilter1.out.dat, colFilter2.out.dat, colFilter1.out.EOF) >>> agg1
    TwineBundle(colFilter1.out.dat, colFilter3.out.dat, colFilter1.out.EOF) >>> agg2
    TwineBundle(agg1.out.reference, agg1.out.dat, agg2.out.dat, agg1.out.EOF) >>> alu

    alu >>> TwineBundle(out.reference, out.dat, out.EOF)
}