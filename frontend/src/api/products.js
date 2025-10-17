export async function getProducts() {
    const resp = await fetch("/api/products/comparison");
    if (!resp.ok) throw new Error("Ошибка загрузки товаров");
    return await resp.json();
}