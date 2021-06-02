package com.zakl.log.aspect;

import com.zakl.log.utils.OutputLogParameter;
import com.zakl.log.annotation.OperationLog;
import com.zakl.log.constant.LogItemNames;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


@Aspect
@Slf4j
public class LogAspect {

//    private final static Logger log = LoggerFactory.getLogger(LogAspect.class);

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);


    @Pointcut("@annotation(com.zakl.log.annotation.OperationLog)")
    public void operationLog() {
    }

    @Around(value = "operationLog()")
    public Object controllerPointCut(ProceedingJoinPoint joinPoint) throws Throwable {
        // 定义返回对象、得到方法需要的参数
        Object[] args = joinPoint.getArgs();
        long startTime = System.currentTimeMillis();
        // 执行方法
        Object result = joinPoint.proceed(args);
        long endTime = System.currentTimeMillis();
        // 打印耗时的信息
        long diffTime = endTime - startTime;


        addOperationLog(joinPoint, args, diffTime, LogItemNames.LogType.GLOBAL);

        return result;
    }

    @Around("operationLog()")
    public Object operationLog(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        long diffTime = endTime - startTime;
        addOperationLog(joinPoint, joinPoint.getArgs(), diffTime, LogItemNames.LogType.BUSINESS);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationLog annotation = signature.getMethod().getAnnotation(OperationLog.class);
        String message = getDetail(joinPoint, annotation, signature);
        MDC.put(LogItemNames.MESSAGE, message);
        MDC.put(LogItemNames.OPERATIONTYPE, annotation.operationType().getValue());
        log.info(message + "\n" + "方法调用响应值----->" + result.toString());
        return result;
    }

    /**
     * 对当前登录用户和占位符处理
     *
     * @return 返回处理后的描述
     */
    private String getDetail(ProceedingJoinPoint joinPoint, OperationLog annotation, MethodSignature signature) {
        StringBuilder sb = new StringBuilder();
        Class[] parameterTypes = signature.getParameterTypes();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        sb.append("执行方法(Operation.detail)：").append(annotation.detail());
        sb.append("\n");
        for (int i = 0; i < parameterNames.length; i++) {
            sb.append("参数类型->:").append(parameterTypes.getClass().toString());
            sb.append("\t\t参数名称->:").append(parameterNames[i]);
            sb.append("\t\t参数值->:").append(args[i]);
            sb.append("\n");
        }
        return sb.toString();
    }

    private void addOperationLog(ProceedingJoinPoint joinPoint, Object[] args, long diffTime, String logType) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        String outArgs = parseArgs(args);
        MDC.put(LogItemNames.TYPE, logType);
        MDC.put(LogItemNames.CLASS, className);
        MDC.put(LogItemNames.METHOD, methodName);
        MDC.put(LogItemNames.ARGS, outArgs);
        MDC.put(LogItemNames.DIFFTIME, String.valueOf(diffTime));
        MDC.put(LogItemNames.DATE, String.valueOf(LocalDate.now()));
        MDC.put(LogItemNames.TIME, LocalTime.now().format(DATE_TIME_FORMAT));
    }


    /**
     * Parse args string.
     *
     * @param args the args
     * @return the string
     */
    private String parseArgs(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : args) {
            // HttpServletRequest,HttpServletResponse不输出
            // 反序列化obj的属性到相应参数中.
            sb.append(obj == null ? "null," : OutputLogParameter.printFields(obj, "", 0) + ",");
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

}
