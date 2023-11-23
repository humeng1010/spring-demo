package com.demo3.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SyncDataAspect {

    @After("execution(* com.demo3.controller.HotelController.addHotel(..))"+
            "||execution(* com.demo3.controller.HotelController.deleteHotel(..))" +
            "||execution(* com.demo3.controller.HotelController.updateHotel(..))")
    public void after(JoinPoint joinPoint){
        Signature signature = joinPoint.getSignature();
        String declaringTypeName = signature.getDeclaringTypeName();
        Object[] args = joinPoint.getArgs();
//        args

    }
}
