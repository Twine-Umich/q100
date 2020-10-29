package q100

import chisel3._
import chisel3.util._
import chisel3.simplechisel._
import chisel3.simplechisel.util._
import scala.collection.mutable.ArrayBuffer

class TileInput(val num_of_col: Int) extends Bundle with Q100Params{
    val dat = Vec(num_of_col, UInt(XLEN.W))
    val EOF = Bool() 
}
class TileOutput(val num_of_col:Int) extends Bundle with Q100Params{
    val dat = Vec(num_of_col,UInt(XLEN.W))
    val EOF = Bool() 
}
class Tile extends SimpleChiselModule with Q100Params{

    val in = IO(Input(new TileInput(NUM_OF_COL)))
    var out = new TileOutput(0)
    val ctrl = IO(new DecoupledIOCtrl(4,4))
    val colSelects = new ArrayBuffer[ColSelect]
    val out_args = new ArrayBuffer[Data]

    val base = COL_SELECTED.length * (CONDITIONS.length-1)

    for(col_num <- COL_SELECTED){
        val colSelect = Module(new ColSelect(NUM_OF_COL, col_num))
        colSelects += colSelect
        in >>> colSelect
    }

    val boolGens = new ArrayBuffer[BoolGen]
    val colFilters = new ArrayBuffer[ColFilter]

    for((boolOp,i) <- CONDITIONS.zipWithIndex){
        val boolGen = Module(new BoolGen( boolOp.op, boolOp.cmp_val))
        boolGens += boolGen
        if(i==0){
            colSelects(COL_SELECTED.indexOf(boolOp.col)) >>> boolGen
        }else{
            colFilters((i-1)*COL_SELECTED.length+COL_SELECTED.indexOf(boolOp.col)) >>> boolGen
        }
        for(j <- 0 until COL_SELECTED.length){
            val colFilter = Module(new ColFilter)
            colFilters += colFilter
            if(j == COL_SELECTED.indexOf(boolOp.col))
                boolGen >>> colFilter
            else if(i==0){
                SimpleChiselBundle(colSelects(j).out.dat, boolGen.out.cmp,boolGen.out.EOF) >>> colFilter
            }else{
                SimpleChiselBundle(colFilters((i-1)*COL_SELECTED.length+j).out.dat, boolGen.out.cmp,boolGen.out.EOF) >>> colFilter
            }
        }
    }

    val alus = new ArrayBuffer[ALU]
    for(aluOp <- ALUOPS){
        val alu = Module(new ALU(aluOp.op))
        alus += alu
        if(CONDITIONS.length == 0){
            SimpleChiselBundle(
                colSelects(COL_SELECTED.indexOf(aluOp.lCol)).out.dat,
                colSelects(COL_SELECTED.indexOf(aluOp.rCol)).out.dat,
                colSelects(COL_SELECTED.indexOf(aluOp.rCol)).out.EOF ) >>> alu
        }else{
            SimpleChiselBundle(
                colFilters(base + COL_SELECTED.indexOf(aluOp.lCol)).out.dat,
                colFilters(base + COL_SELECTED.indexOf(aluOp.rCol)).out.dat,
                colFilters(base + COL_SELECTED.indexOf(aluOp.rCol)).out.EOF ) >>> alu
        }
    }
    GROUPBY match{
        case Some(col)=>{
            val aggregators = new ArrayBuffer[Aggregator]
            if(alus.length == 0){
                if(CONDITIONS.length == 0){
                    for((colselect,i)<- colSelects.zipWithIndex){
                        if(i!= COL_SELECTED.indexOf(col)){
                            val aggregator = Module(new Aggregator)
                            aggregators += aggregator
                            SimpleChiselBundle(
                                colSelects(COL_SELECTED.indexOf(col)).out.dat,
                                colselect.out.dat, colselect.out.EOF) >>> aggregator
                        }
                    }
                }else{
                    for(i <- 0 until COL_SELECTED.length){
                        if(i!= COL_SELECTED.indexOf(col)){
                            val aggregator = Module(new Aggregator)
                            aggregators += aggregator
                            SimpleChiselBundle(
                                colFilters(base + COL_SELECTED.indexOf(col)).out.dat,
                                colFilters(base + i).out.dat, 
                                colFilters(base + i).out.EOF) >>> aggregator
                        }
                    }
                }
            }else{
                if(CONDITIONS.length == 0){
                    for(alu <- alus){
                        val aggregator = Module(new Aggregator)
                        aggregators += aggregator                    
                        SimpleChiselBundle(
                            colSelects(COL_SELECTED.indexOf(col)).out.dat,
                            alu.out.dat, alu.out.EOF) >>> aggregator
                    }
                }else{
                    for(alu <- alus){
                        val aggregator = Module(new Aggregator)
                        aggregators += aggregator                    
                        SimpleChiselBundle(
                            colFilters(base + COL_SELECTED.indexOf(col)).out.dat,
                            alu.out.dat, alu.out.EOF) >>> aggregator
                    }
                }
            }
            out_args += aggregators(0).out.reference
            out_args ++= aggregators.map(x=>x.out.dat) 
            out_args += aggregators(0).out.EOF
        }
        case None => {            
            if(alus.length == 0){
                if(CONDITIONS.length == 0){
                    out_args ++= colSelects.map(x=> x.out.dat)
                    out_args += colSelects(0).out.EOF
                }else{
                    out_args ++= colFilters.takeRight(COL_SELECTED.length).map(x=>x.out.dat)
                    out_args += colFilters(0).out.EOF
                }
            }
            else{
                out_args ++= alus.map(x=>x.out.dat)
                out_args += alus(0).out.EOF
            }
        }
    }
    out = IO(Output(new TileOutput(out_args.length-1)))
    SimpleChiselBundle(out_args) >>> out
}