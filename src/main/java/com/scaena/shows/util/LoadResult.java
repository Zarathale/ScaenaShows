package com.scaena.shows.util;

import java.util.List;
import java.util.Map;

public record LoadResult<T>(Map<String, T> items, List<String> errors) {}
