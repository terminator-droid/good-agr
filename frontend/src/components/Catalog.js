import React, { useEffect, useState } from "react";

export function Catalog() {
    const [products, setProducts] = useState([]);
    const [selected, setSelected] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch("/api/products/comparison")
            .then(res => res.json())
            .then(setProducts)
            .finally(() => setLoading(false));
    }, []);

    const addToCart = pair => {
        let cart = JSON.parse(localStorage.getItem("cart") || "[]");
        cart.push({
            productId: pair.cheaperShop === "SAMOKAT" ? pair.samokatProduct.id : pair.lavkaProduct.id,
            title: pair.productName,
            shop: pair.cheaperShop,
            price: pair.cheaperShop === "SAMOKAT" ? pair.samokatPrice : pair.lavkaPrice,
            quantity: 1
        });
        localStorage.setItem("cart", JSON.stringify(cart));
        alert("Добавлено в корзину!");
        setSelected({ ...selected, [pair.productName]: true });
    };

    if (loading) return <div>Загрузка...</div>;
    return (
        <div>
            <h2>Каталог</h2>
            {products.map(pair => (
                <div key={pair.productName} style={{border: "1px solid #ddd", margin: 10, padding: 10}}>
                    <b>{pair.productName}</b>
                    <div>Самокат: {pair.samokatPrice}₽ | Лавка: {pair.lavkaPrice}₽</div>
                    <div>
            <span style={{color: "#10b981"}}>
              Выгоднее: {pair.cheaperShop} (экономия {pair.priceDifference}₽)
            </span>
                    </div>
                    <button
                        disabled={selected[pair.productName]}
                        onClick={() => addToCart(pair)}
                    >В корзину</button>
                </div>
            ))}
        </div>
    );
}
