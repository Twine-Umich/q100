package q100

import chisel3._
import chisel3.util._
import chisel3.simplechisel._
import chisel3.simplechisel.util._
import scala.collection.mutable.ArrayBuffer

class Tile extends SimpleChiselModule with Q100Params{

    // Declare the top layer inputs
    val in = IO(Input(new TileInput(4)))
    var out = IO(Output(new TileInput(2)))
    val ctrl = IO(new DecoupledIOCtrl(3,3))
    
    // First declare modules
    val colSelect0 = Module(new ColSelect(4, 0))
    val colSelect1 = Module(new ColSelect(4, 1))
    val colSelect2 = Module(new ColSelect(4, 2))

    val boolGen = Module(new BoolGen(BoolGenOP.GT, 6.U))

    val colFilter0 = Module(new ColFilter)
    val colFilter1 = Module(new ColFilter)
    val colFilter2 = Module(new ColFilter)

    val alu = Module(new ALU(AluOp.ADD))

    val aggregate = Module(new Aggregator)

    in >>> colSelect0
    in >>> colSelect1
    in >>> colSelect2

    colSelect0 >>> boolGen

    boolGen >>> colFilter0
    SimpleChiselBundle(colSelect1.out.dat, boolGen.out.cmp, colSelect1.out.EOF) >>> colFilter1
    SimpleChiselBundle(colSelect2.out.dat, boolGen.out.cmp, colSelect2.out.EOF) >>> colFilter2

    SimpleChiselBundle(colFilter1.out.dat, colFilter2.out.dat, colFilter1.out.EOF) >>> alu

    SimpleChiselBundle(colFilter0.out.dat, alu.out.dat, alu.out.EOF) >>> aggregate

    aggregate >>> out
}