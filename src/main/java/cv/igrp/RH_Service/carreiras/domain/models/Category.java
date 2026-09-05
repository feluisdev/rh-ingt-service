package cv.igrp.RH_Service.carreiras.domain.models;

import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

@Getter

public class Category {

    private CategoryId id;
    private CareerId careerId;
    private String code;
    private String name;
    private String description;
    private Integer ordemProgressao;
    private Boolean isActive;

    private Category() {}

    public static Category criar(CareerId careerId, String code, String name, String description,
                                 Integer ordemProgressao) {
        Category category = new Category();
        category.id = CategoryId.gerarNovo();
        category.careerId = careerId;
        category.code = code;
        category.name = name;
        category.description = description;
        category.ordemProgressao = ordemProgressao;
        category.isActive = true;
        return category;
    }

    public static Category reconstituir(CategoryId id, CareerId careerId, String code,
                                        String name, String description,
                                        Integer ordemProgressao, Boolean isActive) {
        Category category = new Category();
        category.id = id;
        category.careerId = careerId;
        category.code = code;
        category.name = name;
        category.description = description;
        category.ordemProgressao = ordemProgressao;
        category.isActive = isActive;
        return category;
    }

    public void atualizar(String name, String description, Integer ordemProgressao) {
        this.name = name;
        this.description = description;
        this.ordemProgressao = ordemProgressao;
    }

    public void desativar() {
        if (Boolean.FALSE.equals(this.isActive)) {
            throw IgrpResponseStatusException.conflict("Categoria já está inativa.");
        }
        this.isActive = false;
    }

    public void reativar() {
        if (Boolean.TRUE.equals(this.isActive)) {
            throw IgrpResponseStatusException.conflict("Categoria já está ativa.");
        }
        this.isActive = true;
    }
}
