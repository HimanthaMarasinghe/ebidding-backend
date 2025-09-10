package com.e.bidding.item_service.repo;

import com.e.bidding.item_service.common.ItemCategory;
import com.e.bidding.item_service.common.ItemState;
import com.e.bidding.item_service.model.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Repository
@SuppressWarnings("unchecked")
public class ItemCustomRepository {

    private final EntityManager entityManager;

    public ItemCustomRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Item> searchItems(
            String searchTerm,
            ItemState status,
            ItemCategory category,
            Integer limit,
            Integer page
    ) {

        if (searchTerm == null || searchTerm.isEmpty() || status == null) {
            System.out.println("searchTerm is empty or status is invalid");
            return null; // or Collections.emptyList() for safety
        }

        if (limit == null || limit < 0) limit = 12;
        if (page == null || page < 0) page = 0;

        StringBuilder sql = getStringBuilder(status, category);

        Query query = entityManager.createNativeQuery(sql.toString(), Item.class);

        // set params
        // turn "Yamah F" → "Yamah:* & F:*"
        String tsQuery = Arrays.stream(searchTerm.trim().split("\\s+"))
                .map(word -> word + ":*")
                .collect(Collectors.joining(" & "));

        query.setParameter("tsQuery", tsQuery);
        query.setParameter("rawTerm", searchTerm);
        
        if (category != null ) {
            query.setParameter("category", category.toString());
        }

        if(status != ItemState.NotScheduled) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            query.setParameter("now", now);
        }

        query.setParameter("limit", limit);
        query.setParameter("page", page);

        return (List<Item>) query.getResultList(); // cast safely
    }

    private static StringBuilder getStringBuilder(ItemState status, ItemCategory category) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( ");
        sql.append("SELECT i.*, ");
        sql.append("ts_rank(order_by_term, to_tsquery('english', :tsQuery)) AS rank, ");
        sql.append("similarity(i.title, :rawTerm) AS sim ");
        sql.append("FROM item i ");

        if (status == ItemState.NotScheduled) {
            sql.append("LEFT JOIN auction a ON i.id = a.id WHERE a.id IS NULL ");
        } else {
            sql.append("JOIN auction a ON i.id = a.id WHERE ");
            switch (status) {
                case Pending:
                    sql.append("a.starting_time >= :now ");
                    break;
                case Active:
                    sql.append("a.starting_time <= :now AND a.ending_time >= :now ");
                    break;
                case Completed:
                    sql.append("a.ending_time <= :now ");
                    break;
            }
        }

        // category condition
        if (category != null) {
            sql.append("AND i.category = :category ");
        }


        sql.append("AND (order_by_term @@ to_tsquery('english', :tsQuery) ");
        sql.append("OR i.title % :rawTerm) ");

        sql.append(") q ");

        // order by
        sql.append("ORDER BY GREATEST(rank, sim) DESC");

        // limit and offset
        sql.append(" LIMIT :limit OFFSET :page");
        return sql;
    }
}
