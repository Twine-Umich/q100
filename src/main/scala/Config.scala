package q100

import chisel3._
import chisel3.util._
import chisel3.simplechisel._
import chisel3.simplechisel.util._
import scala.collection.immutable._

// Supported operations: ADD, MINUS, GT, GET, LT, LET, EQ, NEQ
import AluOp._
class ALU_OP(val lCol:Int, val rCol:Int, val op:AluOp){}

object ALU_OP{
    def apply(lCol:Int, rCol:Int, op:AluOp): ALU_OP = new ALU_OP(lCol, rCol, op)
}

// Supported operations: GT, GET, LT, LET, EQ, NEQ
import BoolGenOP._
class COND_OP(val col:Int, val cmp_val:UInt, val op:BoolGenOP){}

object COND_OP{
    def apply(col:Int, cmp_val:UInt, op:BoolGenOP): COND_OP = new COND_OP(col, cmp_val, op)
}

trait Q100Params{
    val XLEN = 32
    val NUM_OF_COL = 4
    
    val COL_SELECTED = List(0,1,2)
    val CONDITIONS = List(
        COND_OP(1, 3.U, BoolGenOP.GT),
        COND_OP(2, 11.U, BoolGenOP.LT)
    )
    val ALUOPS = List(
        ALU_OP(1,2, AluOp.ADD)
    )
    val GROUPBY:Option[Int] = Some(0)
}