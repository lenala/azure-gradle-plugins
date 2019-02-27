/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.lenala.azure.sample.dao;

import com.lenala.azure.sample.model.TodoItem;
import com.microsoft.azure.spring.data.documentdb.repository.DocumentDbRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoItemRepository extends DocumentDbRepository<TodoItem, String> {
}
