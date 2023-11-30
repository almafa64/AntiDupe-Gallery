use core::panic;

use proc_macro::TokenStream;
use quote::quote;
use syn::{parse_quote, ExprLit, ItemFn, Meta};

#[proc_macro_attribute]
pub fn java_export(attr: TokenStream, input: TokenStream) -> TokenStream {
    let meta: Meta = syn::parse(attr).unwrap();

    let class = {
        let nv = meta.require_name_value().unwrap();
        if nv.path.is_ident("class") {
            use syn::Expr::Lit;
            use syn::Lit::Str;
            if let Lit(ExprLit {
                lit: Str(class_name),
                ..
            }) = &nv.value
            {
                class_name.value()
            } else {
                panic!("class name must be a string literal")
            }
        } else {
            panic!("no class");
        }
    };

    let mut fn_decl: ItemFn = syn::parse(input).unwrap();
    let fn_name = fn_decl.sig.ident.to_string();

    let jni_name = format!(
        "Java_{class_name}_{fn_name}",
        class_name = class.replace('.', "_")
    );

    fn_decl.attrs.push(parse_quote! {
        #[export_name = #jni_name]
    });

    quote! { #fn_decl }.into()
}
