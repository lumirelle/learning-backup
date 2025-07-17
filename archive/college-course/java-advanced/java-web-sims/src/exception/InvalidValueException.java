package exception;

/**
 * BeanDataValidator 检验实体对象的属性值时，出现无效值时抛出的异常
 */
public class InvalidValueException extends RuntimeException {

  public InvalidValueException() {
  }

  public InvalidValueException(String message) {
    super(message);
  }

  public InvalidValueException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidValueException(Throwable cause) {
    super(cause);
  }

  public InvalidValueException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

