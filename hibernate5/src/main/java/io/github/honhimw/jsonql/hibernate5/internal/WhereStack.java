package io.github.honhimw.jsonql.hibernate5.internal;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;

/**
 * @author hon_him
 * @since 2024-01-30
 */

public class WhereStack {

    private final Stack<Element> _stack = new Stack<>();

    private WhereStack() {
    }

    public static WhereStack getInstance() {
        WhereStack whereStack = new WhereStack();
        // root element, avoid empty stack exception
        whereStack.push(true);
        return whereStack;
    }

    public void push(boolean and) {
        _stack.push(new Element(new ArrayList<>(), and));
    }

    public void pop() {
        Element pop = _stack.pop();
        if (!pop.predicates().isEmpty()) {
            BiFunction<Root<?>, CriteriaBuilder, Predicate> merge = merge(pop.predicates(), pop.and());
            Element peek = _stack.peek();
            peek.predicates().add(merge);
        }
    }

    public int size() {
        return _stack.size();
    }

    public BiFunction<Root<?>, CriteriaBuilder, Predicate> peek() {
        Element peek = _stack.peek();
        return merge(peek.predicates(), peek.and());
    }

    public void addPredicate(BiFunction<Root<?>, CriteriaBuilder, Predicate> predicate) {
        _stack.peek().predicates().add(predicate);
    }

    private BiFunction<Root<?>, CriteriaBuilder, Predicate> merge(List<BiFunction<Root<?>, CriteriaBuilder, Predicate>> subPredicates, boolean and) {
        return (root, cb) -> {
            List<Predicate> ps = new ArrayList<>(subPredicates.size());
            for (BiFunction<Root<?>, CriteriaBuilder, Predicate> _subPredicate : subPredicates) {
                Predicate apply = _subPredicate.apply(root, cb);
                ps.add(apply);
            }
            if (and) {
                return cb.and(ps.toArray(Predicate[]::new));
            } else {
                return cb.or(ps.toArray(Predicate[]::new));
            }
        };
    }

    private record Element(List<BiFunction<Root<?>, CriteriaBuilder, Predicate>> predicates, boolean and) {

    }

}
