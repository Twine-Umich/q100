// package q100

// import chisel3._
// import chisel3.util._
// import chisel3.twine._
// import chisel3.twine.util._

// class Tilepaper_Input extends Bundle with Q100Params{
//     val dat = Vec(9, UInt(XLEN.W))
//     val EOF = Bool() 
// }
// class Tilepaper_Output extends Bundle with Q100Params{
//     val reference = Vec(3, UInt(XLEN.W))
//     val dat = Vec(3, UInt(XLEN.W))
//     val EOF =Vec(3,  Bool() )
// }

// class Tile extends TwineModule with Q100Params{

//     // Declare the top layer inputs. Don't Change
//     val in = IO(Input(new Tilepaper_Input))
//     var out = IO(Output(new Tilepaper_Output))
//     val ctrl = IO(new DecoupledIOCtrl(2,2))

//     val agg1 = Module(new Aggregator)
//     val agg2 = Module(new Aggregator)
//     val agg3 = Module(new Aggregator)
//     val agg4 = Module(new Aggregator)
//     val agg5 = Module(new Aggregator)
//     val agg6 = Module(new Aggregator)
// 		val alu1 = Module(new MULTISTAGE_ALU(1))
//     val alu2 = Module(new MULTISTAGE_ALU(1))
//     val alu3 = Module(new MULTISTAGE_ALU(1))
// 		val colSelect1= Module(new ColSelect(9,0))
// 		val colSelect2 = Module(new ColSelect(9,1))
// 		val colSelect3 = Module(new ColSelect(9,2))
// 		val colSelect4 = Module(new ColSelect(9,3))
// 		val colSelect5 = Module(new ColSelect(9,4))
// 		val colSelect6 = Module(new ColSelect(9,5))
// 		val colSelect7 = Module(new ColSelect(9,6))
// 		val colSelect8 = Module(new ColSelect(9,7))
// 		val colSelect9 = Module(new ColSelect(9,8))
// 		in >>> colSelect1
// 		in >>> colSelect2
// 		in >>> colSelect3
// 		in >>> colSelect4
// 		in >>> colSelect5
// 		in >>> colSelect6
// 		in >>> colSelect7
// 		in >>> colSelect8
// 		in >>> colSelect9
// 		val colFilter1 = Module(new ColFilter)
// 		val colFilter2 = Module(new ColFilter)
// 		val colFilter3 = Module(new ColFilter)
// 		val colFilter4 = Module(new ColFilter)
// 		val colFilter5 = Module(new ColFilter)
// 		val colFilter6 = Module(new ColFilter)
// 		val colFilter7 = Module(new ColFilter)
// 		val colFilter8 = Module(new ColFilter)
// 		val colFilter9 = Module(new ColFilter)
// 		val boolGen1 = Module(new BoolGen(BoolGenOP.NEQ, 0.U))
// 		val boolGen2 = Module(new BoolGen(BoolGenOP.NEQ, 0.U))
// 		val boolGen3 = Module(new BoolGen(BoolGenOP.NEQ, 0.U))
// 		TwineBundle(colSelect1.out.dat, colSelect1.out.EOF) >>> boolGen1
// 		TwineBundle(colSelect4.out.dat, colSelect4.out.EOF) >>> boolGen2
// 		TwineBundle(colSelect7.out.dat, colSelect7.out.EOF) >>> boolGen3
// 		boolGen1 >>> colFilter1
// 		boolGen2 >>> colFilter4
// 		boolGen3 >>> colFilter7
// 		TwineBundle(colSelect2.out.dat,boolGen1.out.cmp, colSelect2.out.EOF) >>> colFilter2
// 		TwineBundle(colSelect3.out.dat,boolGen1.out.cmp, colSelect3.out.EOF) >>> colFilter3
// 		TwineBundle(colSelect5.out.dat,boolGen2.out.cmp, colSelect4.out.EOF) >>> colFilter5
// 		TwineBundle(colSelect6.out.dat,boolGen2.out.cmp, colSelect5.out.EOF) >>> colFilter6
// 		TwineBundle(colSelect8.out.dat,boolGen3.out.cmp, colSelect8.out.EOF) >>> colFilter8
// 		TwineBundle(colSelect9.out.dat,boolGen3.out.cmp, colSelect9.out.EOF) >>> colFilter9
//     TwineBundle(colFilter1.out.dat, colFilter2.out.dat, colFilter1.out.EOF) >>> agg1
//     TwineBundle(colFilter1.out.dat, colFilter3.out.dat, colFilter1.out.EOF) >>> agg2
//     TwineBundle(colFilter4.out.dat, colFilter5.out.dat, colFilter4.out.EOF) >>> agg3
//     TwineBundle(colFilter4.out.dat, colFilter6.out.dat, colFilter4.out.EOF) >>> agg4
//     TwineBundle(colFilter7.out.dat, colFilter8.out.dat, colFilter7.out.EOF) >>> agg5
//     TwineBundle(colFilter7.out.dat, colFilter9.out.dat, colFilter7.out.EOF) >>> agg6
//     TwineBundle(agg1.out.reference, agg1.out.dat, agg2.out.dat, agg1.out.EOF) >>> alu1
//     TwineBundle(agg3.out.reference, agg3.out.dat, agg4.out.dat, agg3.out.EOF) >>> alu2
//     TwineBundle(agg5.out.reference, agg5.out.dat, agg6.out.dat, agg5.out.EOF) >>> alu3
//     alu1 >>> TwineBundle(out.reference(0), out.dat(0), out.EOF(0))
//     alu2 >>> TwineBundle(out.reference(1), out.dat(1), out.EOF(1))
//     alu3 >>> TwineBundle(out.reference(2), out.dat(2), out.EOF(2))

// }