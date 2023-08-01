package shadowfox.entity;

public interface IController {
    default void preUpdate() {}
    default void postUpdate() {}
}
