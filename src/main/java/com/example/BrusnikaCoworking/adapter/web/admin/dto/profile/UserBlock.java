package com.example.BrusnikaCoworking.adapter.web.admin.dto.profile;

import com.example.BrusnikaCoworking.domain.reserval.State;

public record UserBlock(Long id,
                        String username,
                        String realname,
                        Integer countBlock,
                        State stateBlock) {

}
