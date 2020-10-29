package q100

import chisel3._
import chisel3.util._
import chisel3.simplechisel._
import chisel3.simplechisel.util._

class AggregatorIO extends Bundle with Q100Params{
    val reference = UInt(XLEN.W)
    val dat = UInt(XLEN.W)
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}

class Aggregator extends SimpleChiselModule with Q100Params{
    val in = IO(Input(new AggregatorIO))
    val out = IO(Output(new AggregatorIO))
    val ctrl = IO(new DecoupledIOCtrl(4,4))

    val crntRef = Reg(UInt(XLEN.W))
    val crntSum = Reg(UInt(XLEN.W))
    val crntValid = Reg(Bool())
    val nextEOF = Reg(Bool())
    // If the current is valid and the refs are different, we may consider push
    val maybePush = crntValid && (crntRef =/= in.reference)
    // if the current is valid and the refs are the same, we may consider accumulate
    val maybeAccumulate = crntValid && (crntRef === in.reference)

    when(ctrl.out.ready){ // when we can push to our outputs freely
        when(nextEOF){
            false.B >>> nextEOF
            true.B >>> ctrl.out.valid
            true.B >>> ctrl.in.ready 
            SimpleChiselBundle(0.U, 0.U, nextEOF) >>> out
        }.otherwise{
            when(ctrl.in.valid){ // There is an input
                when(in.EOF && crntValid){ // Push anyway
                    true.B >>> ctrl.out.valid
                    true.B >>> ctrl.in.ready
                    false.B >>> crntValid
                    0.U >>> crntRef
                    0.U >>> crntSum
                    true.B >>> nextEOF
                    SimpleChiselBundle(crntRef, crntSum, false.B) >>> out
                }.elsewhen(maybePush){
                    true.B >>> ctrl.out.valid
                    true.B >>> ctrl.in.ready
                    SimpleChiselBundle(in.reference, in.dat) >>> SimpleChiselBundle(crntRef, crntSum)
                    SimpleChiselBundle(crntRef, crntSum, false.B) >>> out
                }.elsewhen(maybeAccumulate){
                    false.B >>> ctrl.out.valid
                    true.B >>> ctrl.in.ready
                    (crntSum + in.dat) >>> crntSum
                    SimpleChiselBundle(0.U, 0.U, false.B) >>> out
                }.otherwise{ // this means the current is not valid
                    false.B >>> ctrl.out.valid
                    true.B >>> ctrl.in.ready
                    true.B >>> crntValid
                    in.EOF >>> nextEOF
                    SimpleChiselBundle(in.reference, in.dat) >>> SimpleChiselBundle(crntRef, crntSum)
                    SimpleChiselBundle(0.U, 0.U, false.B) >>> out                
                }
            }.otherwise{ // input not valid, nothing changes
                ctrl.out.valid := false.B
                ctrl.in.ready := true.B
                SimpleChiselBundle(0.U, 0.U, false.B) >>> out
            }
        }
    }.otherwise{ // when we are not able to push
        when(ctrl.in.valid){ // There is an input
            when(maybeAccumulate && (~in.EOF)){ // the only scenario is that we can accumulate or we haven't initialized
                false.B >>> ctrl.out.valid
                true.B >>> ctrl.in.ready
                (crntSum + in.dat) >>> crntSum
                SimpleChiselBundle(0.U, 0.U, false.B) >>> out
            }.elsewhen(~crntValid){
                false.B >>> ctrl.out.valid
                true.B >>> ctrl.in.ready
                true.B >>> crntValid
                in.EOF >>> nextEOF
                SimpleChiselBundle(in.reference, in.dat) >>> SimpleChiselBundle(crntRef, crntSum)
                SimpleChiselBundle(0.U, 0.U, false.B) >>> out        
            }
            .otherwise{ // this means nothing changes
                false.B >>> ctrl.out.valid
                false.B >>> ctrl.in.ready
                SimpleChiselBundle(0.U, 0.U, false.B) >>> out               
            }
        }.otherwise{ // input not valid, nothing changes
            ctrl.out.valid := false.B
            ctrl.in.ready := true.B
            SimpleChiselBundle(0.U, 0.U, false.B) >>> out               
        }
    }
}