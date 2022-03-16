// package q100

// import chisel3._
// import chisel3.util._
// import chisel3.twine._
// import chisel3.twine.util._
// import scala.collection.mutable.ArrayBuffer

// class TileInput(val num_of_col: Int) extends Bundle with Q100Params{
//     val dat = Vec(num_of_col, UInt(XLEN.W))
//     val EOF = Bool() 
// }
// class TileOutput(val num_of_col:Int) extends Bundle with Q100Params{
//     val dat = Vec(num_of_col,UInt(XLEN.W))
//     val EOF = Bool() 
// }
// class Tile extends TwineModule with Q100Params{
//     var num_of_lines = 3
//     var c_num_of_lines = 5
//     var num_of_components = 0
//     val in = IO(Input(new TileInput(NUM_OF_COL)))
//     var out = new TileOutput(0)
//     val ctrl = IO(new DecoupledIOCtrl(4,4))
//     val colSelects = new ArrayBuffer[ColSelect]
//     val out_args = new ArrayBuffer[Data]

//     val base = COL_SELECTED.length * (CONDITIONS.length-1)

//     for(col_num <- COL_SELECTED){
//         val colSelect = Module(new ColSelect(NUM_OF_COL, col_num))
//         colSelects += colSelect
//         in >>> colSelect
//         num_of_lines += 2
//         c_num_of_lines += 5
//         num_of_components += 1
//     }

//     val boolGens = new ArrayBuffer[BoolGen]
//     val colFilters = new ArrayBuffer[ColFilter]

//     for((boolOp, i) <- CONDITIONS.zipWithIndex){
//         val boolGen = Module(new BoolGen( boolOp.op, boolOp.cmp_val))
//         boolGens += boolGen
//         num_of_lines += 2
//         c_num_of_lines += 3
//         num_of_components += 1
//         if(i==0){
//             c_num_of_lines += 2
//             colSelects(COL_SELECTED.indexOf(boolOp.col)) >>> boolGen
//         }else{
//             c_num_of_lines += 2
//             colFilters((i-1)*COL_SELECTED.length+COL_SELECTED.indexOf(boolOp.col)) >>> boolGen
//         }
//         for(j <- 0 until COL_SELECTED.length){
//             val colFilter = Module(new ColFilter)
//             num_of_components += 1
//             colFilters += colFilter
//             num_of_lines += 2
//             c_num_of_lines += 6
//             if(j == COL_SELECTED.indexOf(boolOp.col)){
//                 boolGen >>> colFilter
//             }else if(i==0){
//                 TwineBundle(colSelects(j).out.dat, boolGen.out.cmp,boolGen.out.EOF) >>> colFilter
//             }else{
//                 TwineBundle(colFilters((i-1)*COL_SELECTED.length+j).out.dat, boolGen.out.cmp,boolGen.out.EOF) >>> colFilter
//             }
//         }
//     }

//     val alus = new ArrayBuffer[ALU]
//     for(aluOp <- ALUOPS){
//         val alu = Module(new ALU(aluOp.op))
//         num_of_components += 1
//         alus += alu
//         num_of_lines += 2
//         c_num_of_lines += 6
//         if(CONDITIONS.length == 0){
//             TwineBundle(
//                 colSelects(COL_SELECTED.indexOf(aluOp.lCol)).out.dat,
//                 colSelects(COL_SELECTED.indexOf(aluOp.rCol)).out.dat,
//                 colSelects(COL_SELECTED.indexOf(aluOp.rCol)).out.EOF ) >>> alu
//         }else{
//             TwineBundle(
//                 colFilters(base + COL_SELECTED.indexOf(aluOp.lCol)).out.dat,
//                 colFilters(base + COL_SELECTED.indexOf(aluOp.rCol)).out.dat,
//                 colFilters(base + COL_SELECTED.indexOf(aluOp.rCol)).out.EOF ) >>> alu
//         }
//     }
//     GROUPBY match{
//         case Some(col)=>{
//             val aggregators = new ArrayBuffer[Aggregator]
//             if(alus.length == 0){
//                 if(CONDITIONS.length == 0){
//                     for((colselect,i)<- colSelects.zipWithIndex){
//                         if(i!= COL_SELECTED.indexOf(col)){
//                             val aggregator = Module(new Aggregator)
//                             num_of_components += 1
//                             num_of_lines += 2
//                             c_num_of_lines += 6
//                             aggregators += aggregator
//                             TwineBundle(
//                                 colSelects(COL_SELECTED.indexOf(col)).out.dat,
//                                 colselect.out.dat, colselect.out.EOF) >>> aggregator
//                         }
//                     }
//                 }else{
//                     for(i <- 0 until COL_SELECTED.length){
//                         if(i!= COL_SELECTED.indexOf(col)){
//                             val aggregator = Module(new Aggregator)
//                             aggregators += aggregator
//                             num_of_lines += 2
//                             c_num_of_lines += 6
//                             num_of_components += 1
//                             TwineBundle(
//                                 colFilters(base + COL_SELECTED.indexOf(col)).out.dat,
//                                 colFilters(base + i).out.dat, 
//                                 colFilters(base + i).out.EOF) >>> aggregator
//                         }
//                     }
//                 }
//             }else{
//                 if(CONDITIONS.length == 0){
//                     for(alu <- alus){
//                         val aggregator = Module(new Aggregator)
//                         num_of_components += 1
//                         aggregators += aggregator             
//                         num_of_lines += 2  
//                         c_num_of_lines += 6     
//                         TwineBundle(
//                             colSelects(COL_SELECTED.indexOf(col)).out.dat,
//                             alu.out.dat, alu.out.EOF) >>> aggregator
//                     }
//                 }else{
//                     for(alu <- alus){
//                         val aggregator = Module(new Aggregator)
//                         num_of_components += 1
//                         aggregators += aggregator    
//                         num_of_lines += 2
//                         c_num_of_lines += 6               
//                         TwineBundle(
//                             colFilters(base + COL_SELECTED.indexOf(col)).out.dat,
//                             alu.out.dat, alu.out.EOF) >>> aggregator
//                     }
//                 }
//             }
//             out_args += aggregators(0).out.reference
//             out_args ++= aggregators.map(x=>x.out.dat) 
//             out_args += aggregators(0).out.EOF
//         }
//         case None => {            
//             if(alus.length == 0){
//                 if(CONDITIONS.length == 0){
//                     out_args ++= colSelects.map(x=> x.out.dat)
//                     out_args += colSelects(0).out.EOF
//                 }else{
//                     out_args ++= colFilters.takeRight(COL_SELECTED.length).map(x=>x.out.dat)
//                     out_args += colFilters(0).out.EOF
//                 }
//             }
//             else{
//                 out_args ++= alus.map(x=>x.out.dat)
//                 out_args += alus(0).out.EOF
//             }
//         }
//     }
//     out = IO(Output(new TileOutput(out_args.length-1)))
//     num_of_lines += 1
//     c_num_of_lines += out_args.length
//     TwineBundle(out_args) >>> out
//     Console.println(s"num_of_lines: ${num_of_lines}")
//     Console.println(s"c_num_of_lines: ${c_num_of_lines}")
//     Console.println(s"num_of_components: ${num_of_components}")

// }