package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRolesDto {
    private List<String> add;
    private List<String> remove;
}
