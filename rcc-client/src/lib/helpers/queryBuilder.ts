export const buildPaginationQuery = (params: {
    page?: number;
    size?: number;
    search?: string;
    searchKey?: string;
}): string => {
    const { page = 0, size = 10, search, searchKey = 'query' } = params;
    
    if (search) {
        return `?${searchKey}=${encodeURIComponent(search)}`;
    }
    return `?size=${size}&page=${page}`;
};
